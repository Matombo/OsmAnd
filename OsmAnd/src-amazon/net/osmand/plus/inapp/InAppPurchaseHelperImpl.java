package net.osmand.plus.inapp;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserData;

import net.osmand.AndroidUtils;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.inapp.InAppPurchases.InAppPurchase;
import net.osmand.plus.inapp.InAppPurchases.InAppSubscription;
import net.osmand.plus.inapp.InAppPurchases.PurchaseInfo;
import net.osmand.plus.inapp.util.IapManager;
import net.osmand.plus.inapp.util.IapPurchasingListener;
import net.osmand.plus.inapp.util.IapPurchasingListener.PurchaseResponseListener;
import net.osmand.plus.inapp.util.UserIapData;
import net.osmand.plus.settings.backend.OsmandSettings;
import net.osmand.util.Algorithms;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InAppPurchaseHelperImpl extends InAppPurchaseHelper {

	private final IapPurchasingListener purchasingListener;

	private UserIapData userData;
	private Map<String, Product> productMap = new HashMap<>();
	private List<Receipt> receipts = new ArrayList<>();

	public InAppPurchaseHelperImpl(OsmandApplication ctx) {
		super(ctx);
		purchases = new InAppPurchasesImpl(ctx);

		IapManager iapManager = new IapManager();
		purchasingListener = new IapPurchasingListener(iapManager);
		PurchasingService.registerListener(ctx, purchasingListener);
	}

	@Override
	public void isInAppPurchaseSupported(@NonNull Activity activity, @Nullable InAppPurchaseInitCallback callback) {
		if (callback != null) {
			callback.onSuccess();
		}
	}

	@Override
	protected void execImpl(@NonNull InAppPurchaseTaskType taskType, @NonNull InAppCommand command) {

	}

	@Override
	public void purchaseFullVersion(@NonNull Activity activity) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Attempt to purchase full version (amazon build)");
	}

	@Override
	public void purchaseDepthContours(@NonNull Activity activity) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Attempt to purchase depth contours (amazon build)");
	}

	@Override
	public void purchaseContourLines(@NonNull Activity activity) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Attempt to purchase contour lines (amazon build)");
	}

	@Override
	public void manageSubscription(@NonNull Context ctx, @Nullable String sku) {

	}

	@Override
	protected InAppCommand getPurchaseSubscriptionCommand(WeakReference<Activity> activity, String sku, String userInfo) throws UnsupportedOperationException {
		return new PurchaseSubscriptionCommand(activity, sku);
	}

	@Override
	protected InAppCommand getRequestInventoryCommand(boolean userRequested) throws UnsupportedOperationException {
		return new RequestInventoryCommand(userRequested);
	}

	@Override
	protected boolean isBillingManagerExists() {
		return false;
	}

	@Override
	protected void destroyBillingManager() {
	}

	@Nullable
	private Product getProductInfo(@NonNull String productId) {
		Collection<Product> products = productMap.values();
		for (Product product : products) {
			if (product.getSku().equals(productId)) {
				return product;
			}
		}
		return null;
	}

	// Call when a purchase is finished
	private void onPurchaseFinished(@NonNull Receipt receipt) {
		logDebug("Purchase finished: " + receipt.getSku());
		onPurchaseDone(getPurchaseInfo(receipt));
	}

	private boolean hasDetails(@NonNull String productId) {
		return getProductInfo(productId) != null;
	}

	@Nullable
	private Receipt getReceipt(@NonNull String productId) {
		for (Receipt receipt : receipts) {
			if (receipt.getSku().equals(productId)) {
				return receipt;
			}
		}
		return null;
	}

	private PurchaseInfo getPurchaseInfo(Receipt receipt) {
		return new PurchaseInfo(receipt.getSku(), receipt.getReceiptId(), "",
				receipt.getPurchaseDate().getTime(), 0, true, !receipt.isCanceled());
	}

	private void fetchInAppPurchase(@NonNull InAppPurchase inAppPurchase, @NonNull Product product, @Nullable Receipt receipt) {
		if (receipt != null) {
			inAppPurchase.setPurchaseState(InAppPurchase.PurchaseState.PURCHASED);
			inAppPurchase.setPurchaseInfo(ctx, getPurchaseInfo(receipt));
		} else {
			inAppPurchase.setPurchaseState(InAppPurchase.PurchaseState.NOT_PURCHASED);
			inAppPurchase.restorePurchaseInfo(ctx);
		}
		String price = product.getPrice();
		inAppPurchase.setPrice(price);

		double priceValue = 0;
		String currencyCode = "";
		Pattern regex = Pattern.compile("\\d[\\d,.]+");
		Matcher finder = regex.matcher(price);
		if (finder.find() && finder.groupCount() > 0) {
			try {
				String rawPrice = finder.group(0);
				if (!Algorithms.isEmpty(rawPrice)) {
					priceValue = Double.parseDouble(rawPrice.trim().replaceAll(",", "."));
					currencyCode = price.replaceAll(rawPrice, "").trim();
				}
				// do something with value
			} catch (NumberFormatException e) {
				priceValue = 0;
			}
		}
		inAppPurchase.setPriceCurrencyCode(currencyCode);
		if (priceValue > 0) {
			inAppPurchase.setPriceValue(priceValue);
		}
		if (inAppPurchase instanceof InAppSubscription) {
			String subscriptionPeriod = null;
			if (product.getSku().contains(".annual.")) {
				subscriptionPeriod = "P1Y";
			} else if (product.getSku().contains(".monthly.")) {
				subscriptionPeriod = "P1M";
			}
			if (!Algorithms.isEmpty(subscriptionPeriod)) {
				try {
					((InAppSubscription) inAppPurchase).setSubscriptionPeriodString(subscriptionPeriod);
				} catch (ParseException e) {
					LOG.error(e);
				}
			}
		}
	}

	private class RequestInventoryCommand extends InAppCommand {

		private final boolean userRequested;
		private final PurchaseResponseListener responseListener;

		private RequestInventoryCommand(boolean userRequested) {
			this.userRequested = userRequested;
			this.responseListener = new PurchaseResponseListener() {
				@Override
				public void onUserDataResponse(@Nullable UserIapData userData) {
					InAppPurchaseHelperImpl.this.userData = userData;
					if (userData != null) {
						logDebug("getPurchaseUpdates");
						PurchasingService.getPurchaseUpdates(true);
					} else {
						obtainSubscriptionsInfo(null);
					}
				}

				@Override
				public void onPurchaseUpdatesResponse(@Nullable Map<UserData, List<Receipt>> purchaseMap, boolean hasMore) {
					List<Receipt> receipts = new ArrayList<>(InAppPurchaseHelperImpl.this.receipts);
					UserIapData userData = InAppPurchaseHelperImpl.this.userData;
					if (!Algorithms.isEmpty(purchaseMap) && userData != null) {
						for (Entry<UserData, List<Receipt>> receiptsEntry : purchaseMap.entrySet()) {
							UserData ud = receiptsEntry.getKey();
							if (Algorithms.stringsEqual(userData.getAmazonUserId(), ud.getUserId()) &&
									Algorithms.stringsEqual(userData.getAmazonMarketplace(), ud.getMarketplace())) {
								receipts.addAll(receiptsEntry.getValue());
							}
						}
					}
					InAppPurchaseHelperImpl.this.receipts = receipts;
					if (!hasMore) {
						List<String> skus = new ArrayList<>();
						for (Receipt receipt : receipts) {
							skus.add(receipt.getSku());
						}
						obtainSubscriptionsInfo(skus);
					}
				}

				@Override
				public void onProductDataResponse(@Nullable Map<String, Product> productMap) {
					InAppPurchaseHelperImpl.this.productMap = productMap;
					if (productMap != null) {
						purchasingListener.removeResponseListener(responseListener);
						processInventory();
					} else {
						commandDone();
					}
				}

				@Override
				public void onPurchaseResponse(@Nullable Receipt receipt) {
				}

				private void processInventory() {
					logDebug("Query sku details was successful.");

					/*
					 * Check for items we own. Notice that for each purchase, we check
					 * the developer payload to see if it's correct!
					 */

					List<String> allOwnedSubscriptionSkus = new ArrayList<>();
					for (Receipt receipt : receipts) {
						allOwnedSubscriptionSkus.add(receipt.getSku());
					}
					for (InAppSubscription s : getSubscriptions().getAllSubscriptions()) {
						if (hasDetails(s.getSku())) {
							Receipt receipt = getReceipt(s.getSku());
							Product productInfo = getProductInfo(s.getSku());
							if (productInfo != null) {
								fetchInAppPurchase(s, productInfo, receipt);
							}
							allOwnedSubscriptionSkus.remove(s.getSku());
						}
					}
					for (String sku : allOwnedSubscriptionSkus) {
						Receipt receipt = getReceipt(sku);
						Product productInfo = getProductInfo(sku);
						if (productInfo != null) {
							InAppSubscription s = getSubscriptions().upgradeSubscription(sku);
							if (s != null) {
								fetchInAppPurchase(s, productInfo, receipt);
							}
						}
					}

					// Do we have the live updates?
					boolean subscribedToLiveUpdates = false;
					boolean subscribedToOsmAndPro = false;
					List<Receipt> subscriptionPurchases = new ArrayList<>();
					for (InAppSubscription s : getSubscriptions().getAllSubscriptions()) {
						Receipt receipt = getReceipt(s.getSku());
						if (receipt != null || s.getState().isActive()) {
							if (receipt != null) {
								subscriptionPurchases.add(receipt);
							}
							if (!subscribedToLiveUpdates && purchases.isLiveUpdatesSubscription(s)) {
								subscribedToLiveUpdates = true;
							}
							if (!subscribedToOsmAndPro && purchases.isOsmAndProSubscription(s)) {
								subscribedToOsmAndPro = true;
							}
						}
					}
					if (!subscribedToLiveUpdates && ctx.getSettings().LIVE_UPDATES_PURCHASED.get()) {
						ctx.getSettings().LIVE_UPDATES_PURCHASED.set(false);
						if (!subscribedToOsmAndPro) {
							onSubscriptionExpired();
						}
					} else if (subscribedToLiveUpdates) {
						ctx.getSettings().LIVE_UPDATES_PURCHASED.set(true);
					}
					if (!subscribedToOsmAndPro && ctx.getSettings().OSMAND_PRO_PURCHASED.get()) {
						ctx.getSettings().OSMAND_PRO_PURCHASED.set(false);
						if (!subscribedToLiveUpdates) {
							onSubscriptionExpired();
						}
					} else if (subscribedToOsmAndPro) {
						ctx.getSettings().OSMAND_PRO_PURCHASED.set(true);
					}

					lastValidationCheckTime = System.currentTimeMillis();
					logDebug("User " + (subscribedToLiveUpdates ? "HAS" : "DOES NOT HAVE")
							+ " live updates purchased.");

					OsmandSettings settings = ctx.getSettings();
					settings.INAPPS_READ.set(true);

					List<Receipt> tokensToSend = new ArrayList<>();
					if (subscriptionPurchases.size() > 0) {
						List<String> tokensSent = Arrays.asList(settings.BILLING_PURCHASE_TOKENS_SENT.get().split(";"));
						for (Receipt receipt : subscriptionPurchases) {
							if (!tokensSent.contains(receipt.getSku())) {
								tokensToSend.add(receipt);
							}
						}
					}
					List<PurchaseInfo> purchaseInfoList = new ArrayList<>();
					for (Receipt receipt : tokensToSend) {
						purchaseInfoList.add(getPurchaseInfo(receipt));
					}
					onSkuDetailsResponseDone(purchaseInfoList, userRequested);
				}

				private void onSubscriptionExpired() {
					if (!isDepthContoursPurchased(ctx)) {
						ctx.getSettings().getCustomRenderBooleanProperty("depthContours").set(false);
					}
				}
			};
			purchasingListener.addResponseListener(responseListener);
		}

		@Override
		protected void commandDone() {
			super.commandDone();
			purchasingListener.removeResponseListener(responseListener);
			inventoryRequested = false;
		}

		@Override
		protected boolean userRequested() {
			return userRequested;
		}

		@Override
		public void run(InAppPurchaseHelper helper) {
			logDebug("Setup successful. Querying inventory.");
			try {
				logDebug("getUserData");
				PurchasingService.getUserData();
			} catch (Exception e) {
				logError("queryInventoryAsync Error", e);
				notifyDismissProgress(InAppPurchaseTaskType.REQUEST_INVENTORY);
				stop(true);
				commandDone();
			}
		}

		private void obtainSubscriptionsInfo(@Nullable List<String> skus) {
			if (uiActivity != null) {
				Set<String> productIds = new HashSet<>();
				List<InAppSubscription> subscriptions = purchases.getSubscriptions().getAllSubscriptions();
				for (InAppSubscription s : subscriptions) {
					productIds.add(s.getSku());
				}
				if (skus != null) {
					productIds.addAll(skus);
				}
				PurchasingService.getProductData(productIds);
			} else {
				commandDone();
			}
		}
	}

	private class PurchaseSubscriptionCommand extends InAppCommand {

		private final WeakReference<Activity> activityRef;
		private final String sku;
		private final PurchaseResponseListener responseListener;

		public PurchaseSubscriptionCommand(WeakReference<Activity> activity, String sku) {
			this.activityRef = activity;
			this.sku = sku;

			responseListener = new PurchaseResponseListener() {
				@Override
				public void onUserDataResponse(@Nullable UserIapData userData) {
				}

				@Override
				public void onProductDataResponse(@Nullable Map<String, Product> productMap) {
				}

				@Override
				public void onPurchaseUpdatesResponse(@Nullable Map<UserData, List<Receipt>> purchaseMap, boolean hasMore) {
				}

				@Override
				public void onPurchaseResponse(@Nullable Receipt receipt) {
					if (receipt != null) {
						Activity a = activity.get();
						if (AndroidUtils.isActivityNotDestroyed(a)) {
							onPurchaseFinished(receipt);
						} else {
							logError("startResolutionForResult on destroyed activity");
						}
					} else {
						logError("Purchase failed");
					}
					commandDone();
				}
			};
			purchasingListener.addResponseListener(responseListener);
		}

		@Override
		protected void commandDone() {
			super.commandDone();
			purchasingListener.removeResponseListener(responseListener);
		}

		@Override
		public void run(InAppPurchaseHelper helper) {
			try {
				Activity a = activityRef.get();
				Product productInfo = getProductInfo(sku);
				if (AndroidUtils.isActivityNotDestroyed(a) && productInfo != null) {
					logDebug("getPurchaseUpdates");
					PurchasingService.purchase(sku);
				} else {
					stop(true);
				}
			} catch (Exception e) {
				logError("launchPurchaseFlow Error", e);
				stop(true);
			}
		}
	}
}