// Generated by delombok at Thu Nov 14 21:27:17 PST 2024
/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exchange.core2.core.common.api;

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;

public final class ApiPlaceOrder extends ApiCommand {
    public final long price;
    public final long size;
    public final long orderId;
    public final OrderAction action;
    public final OrderType orderType;
    public final long uid;
    public final int symbol;
    public final int userCookie;
    public final long reservePrice;

    @Override
    public String toString() {
        return "[ADD o" + orderId + " s" + symbol + " u" + uid + " " + (action == OrderAction.ASK ? 'A' : 'B') + ":" + (orderType == OrderType.IOC ? "IOC" : "GTC") + ":" + price + ":" + size + "]";
        //(reservePrice != 0 ? ("(R" + reservePrice + ")") : "") +
    }


    @java.lang.SuppressWarnings("all")
    public static class ApiPlaceOrderBuilder {
        @java.lang.SuppressWarnings("all")
        private long price;
        @java.lang.SuppressWarnings("all")
        private long size;
        @java.lang.SuppressWarnings("all")
        private long orderId;
        @java.lang.SuppressWarnings("all")
        private OrderAction action;
        @java.lang.SuppressWarnings("all")
        private OrderType orderType;
        @java.lang.SuppressWarnings("all")
        private long uid;
        @java.lang.SuppressWarnings("all")
        private int symbol;
        @java.lang.SuppressWarnings("all")
        private int userCookie;
        @java.lang.SuppressWarnings("all")
        private long reservePrice;

        @java.lang.SuppressWarnings("all")
        ApiPlaceOrderBuilder() {
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder price(final long price) {
            this.price = price;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder size(final long size) {
            this.size = size;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder orderId(final long orderId) {
            this.orderId = orderId;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder action(final OrderAction action) {
            this.action = action;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder orderType(final OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder uid(final long uid) {
            this.uid = uid;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder symbol(final int symbol) {
            this.symbol = symbol;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder userCookie(final int userCookie) {
            this.userCookie = userCookie;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder.ApiPlaceOrderBuilder reservePrice(final long reservePrice) {
            this.reservePrice = reservePrice;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public ApiPlaceOrder build() {
            return new ApiPlaceOrder(this.price, this.size, this.orderId, this.action, this.orderType, this.uid, this.symbol, this.userCookie, this.reservePrice);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public java.lang.String toString() {
            return "ApiPlaceOrder.ApiPlaceOrderBuilder(price=" + this.price + ", size=" + this.size + ", orderId=" + this.orderId + ", action=" + this.action + ", orderType=" + this.orderType + ", uid=" + this.uid + ", symbol=" + this.symbol + ", userCookie=" + this.userCookie + ", reservePrice=" + this.reservePrice + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    public static ApiPlaceOrder.ApiPlaceOrderBuilder builder() {
        return new ApiPlaceOrder.ApiPlaceOrderBuilder();
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ApiPlaceOrder)) return false;
        final ApiPlaceOrder other = (ApiPlaceOrder) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.price != other.price) return false;
        if (this.size != other.size) return false;
        if (this.orderId != other.orderId) return false;
        final java.lang.Object this$action = this.action;
        final java.lang.Object other$action = other.action;
        if (this$action == null ? other$action != null : !this$action.equals(other$action)) return false;
        final java.lang.Object this$orderType = this.orderType;
        final java.lang.Object other$orderType = other.orderType;
        if (this$orderType == null ? other$orderType != null : !this$orderType.equals(other$orderType)) return false;
        if (this.uid != other.uid) return false;
        if (this.symbol != other.symbol) return false;
        if (this.userCookie != other.userCookie) return false;
        if (this.reservePrice != other.reservePrice) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ApiPlaceOrder;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $price = this.price;
        result = result * PRIME + (int) ($price >>> 32 ^ $price);
        final long $size = this.size;
        result = result * PRIME + (int) ($size >>> 32 ^ $size);
        final long $orderId = this.orderId;
        result = result * PRIME + (int) ($orderId >>> 32 ^ $orderId);
        final java.lang.Object $action = this.action;
        result = result * PRIME + ($action == null ? 43 : $action.hashCode());
        final java.lang.Object $orderType = this.orderType;
        result = result * PRIME + ($orderType == null ? 43 : $orderType.hashCode());
        final long $uid = this.uid;
        result = result * PRIME + (int) ($uid >>> 32 ^ $uid);
        result = result * PRIME + this.symbol;
        result = result * PRIME + this.userCookie;
        final long $reservePrice = this.reservePrice;
        result = result * PRIME + (int) ($reservePrice >>> 32 ^ $reservePrice);
        return result;
    }

    @java.lang.SuppressWarnings("all")
    public ApiPlaceOrder(final long price, final long size, final long orderId, final OrderAction action, final OrderType orderType, final long uid, final int symbol, final int userCookie, final long reservePrice) {
        this.price = price;
        this.size = size;
        this.orderId = orderId;
        this.action = action;
        this.orderType = orderType;
        this.uid = uid;
        this.symbol = symbol;
        this.userCookie = userCookie;
        this.reservePrice = reservePrice;
    }
}
