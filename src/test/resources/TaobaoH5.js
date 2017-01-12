/**
 * Created by xuanlubin on 2016/6/28.
 * 淘宝加密js
 */
"use strict";

var _base_options = {
    "H5Request": true,
    "useAlipayJSBridge": false,
    "mainDomain": "taobao.com",
    "subDomain": "m",
    "prefix": "api",
    "getJSONP": true
};


var api = {
    rate: "mtop.wdetail.getItemRates",
    detail: "wdetail",
    tb_item_list: "com.taobao.search.api.getShopItemList"
};

function extendDeep(parent, child) {
    child = child || {};
    for (var i in parent) {
        if (parent.hasOwnProperty(i)) {
            if (typeof parent[i] === "object") {
                child[i] = (Object.prototype.toString.call(parent[i]) === "[object Array]") ? [] : {};
                extendDeep(parent[i], child[i]);
            } else {
                child[i] = parent[i];
            }
        }
    }
    return child;
}

var _config = {
    "mtop.wdetail.getItemRates": {
        params: {
            "api": "mtop.wdetail.getItemRates",
            "v": "3.0",
            "AntiCreep": true,
            "data": {
                "hasRateContent": 1,
                "hasPic": 1,
                "auctionNumId": "523322511754",
                "pageSize": 1000,
                "pageNo": 2,
                "rateType": ""
            },
            "type": "get",
            "dataType": "jsonp"
        },
        options: extendDeep(_base_options, {
            "AntiCreep": true,
            "path": "//api.m.taobao.com/h5/mtop.wdetail.getitemrates/3.0/"
        })
    },
    "wdetail": {
        params: {
            "api": "mtop.taobao.detail.getdetail",
            "v": "6.0",
            "ttid": "2013@taobao_h5_1.0.0",
            "data": {
                "itemNumId": "523322511754",
                "exParams": {"spm": "a230r.1.14.25.JINqVh", "id": "523322511754", "ns": "1", "abbucket": "1"}
            },
            "type": "get",
            "dataType": "jsonp"
        },
        options: extendDeep(_base_options, {
            "path": "//api.m.taobao.com/h5/mtop.taobao.detail.getdetail/6.0/"
        })
    },
    "com.taobao.search.api.getShopItemList": {
        params: {
            "api": "com.taobao.search.api.getShopItemList",
            "v": "2.0",
            "data": {
                "shopId": "57692400",
                "currentPage": 1,
                "pageSize": "30",
                "sort": "hotsell",
                "q": ""
            },
            "type": "get",
            "dataType": "jsonp"
        },
        options: extendDeep(_base_options, {
            "WindVaneRequest": false,
            "AntiCreep": true,
            "path": "//api.m.taobao.com/h5/com.taobao.search.api.getshopitemlist/2.0/"
        })
    }
};

var _tb_query = function (apiAction, params) {
    var queryConfig = extendDeep(_config[apiAction]);
    switch (apiAction) {
        case api.rate:
            queryConfig.params.data.auctionNumId = params.id;
            queryConfig.params.data.pageNo = params.page || 1;
            queryConfig.params.data.pageSize = params.pageSize || 1000;
            break;
        case api.tb_item_list:
            queryConfig.params.data.shopId = params.id;
            queryConfig.params.data.currentPage = params.page || 1;
            break;
        default :
            queryConfig.params.data.itemNumId = params.id;
            queryConfig.params.data.exParams.id = params.id;
    }
    return queryConfig;
};

var _tb_sign = function (a) {
    function b(a, b) {
        return a << b | a >>> 32 - b
    }

    function c(a, b) {
        var c, d, e, f, g;
        return e = 2147483648 & a,
            f = 2147483648 & b,
            c = 1073741824 & a,
            d = 1073741824 & b,
            g = (1073741823 & a) + (1073741823 & b),
            c & d ? 2147483648 ^ g ^ e ^ f : c | d ? 1073741824 & g ? 3221225472 ^ g ^ e ^ f : 1073741824 ^ g ^ e ^ f : g ^ e ^ f
    }

    function d(a, b, c) {
        return a & b | ~a & c
    }

    function e(a, b, c) {
        return a & c | b & ~c
    }

    function f(a, b, c) {
        return a ^ b ^ c
    }

    function g(a, b, c) {
        return b ^ (a | ~c)
    }

    function h(a, e, f, g, h, i, j) {
        return a = c(a, c(c(d(e, f, g), h), j)),
            c(b(a, i), e)
    }

    function i(a, d, f, g, h, i, j) {
        return a = c(a, c(c(e(d, f, g), h), j)),
            c(b(a, i), d)
    }

    function j(a, d, e, g, h, i, j) {
        return a = c(a, c(c(f(d, e, g), h), j)),
            c(b(a, i), d)
    }

    function k(a, d, e, f, h, i, j) {
        return a = c(a, c(c(g(d, e, f), h), j)),
            c(b(a, i), d)
    }

    function l(a) {
        for (var b, c = a.length, d = c + 8, e = (d - d % 64) / 64, f = 16 * (e + 1), g = new Array(f - 1), h = 0, i = 0; c > i;)
            b = (i - i % 4) / 4,
                h = i % 4 * 8,
                g[b] = g[b] | a.charCodeAt(i) << h,
                i++;
        return b = (i - i % 4) / 4,
            h = i % 4 * 8,
            g[b] = g[b] | 128 << h,
            g[f - 2] = c << 3,
            g[f - 1] = c >>> 29,
            g
    }

    function m(a) {
        var b, c, d = "", e = "";
        for (c = 0; 3 >= c; c++)
            b = a >>> 8 * c & 255,
                e = "0" + b.toString(16),
                d += e.substr(e.length - 2, 2);
        return d
    }

    function n(a) {
        a = a.replace(/\r\n/g, "\n");
        for (var b = "", c = 0; c < a.length; c++) {
            var d = a.charCodeAt(c);
            128 > d ? b += String.fromCharCode(d) : d > 127 && 2048 > d ? (b += String.fromCharCode(d >> 6 | 192),
                b += String.fromCharCode(63 & d | 128)) : (b += String.fromCharCode(d >> 12 | 224),
                b += String.fromCharCode(d >> 6 & 63 | 128),
                b += String.fromCharCode(63 & d | 128))
        }
        return b
    }

    var o, p, q, r, s, t, u, v, w, x = [], y = 7, z = 12, A = 17, B = 22, C = 5, D = 9, E = 14, F = 20, G = 4, H = 11, I = 16, J = 23, K = 6, L = 10, M = 15, N = 21;
    for (a = n(a),
             x = l(a),
             t = 1732584193,
             u = 4023233417,
             v = 2562383102,
             w = 271733878,
             o = 0; o < x.length; o += 16)
        p = t,
            q = u,
            r = v,
            s = w,
            t = h(t, u, v, w, x[o + 0], y, 3614090360),
            w = h(w, t, u, v, x[o + 1], z, 3905402710),
            v = h(v, w, t, u, x[o + 2], A, 606105819),
            u = h(u, v, w, t, x[o + 3], B, 3250441966),
            t = h(t, u, v, w, x[o + 4], y, 4118548399),
            w = h(w, t, u, v, x[o + 5], z, 1200080426),
            v = h(v, w, t, u, x[o + 6], A, 2821735955),
            u = h(u, v, w, t, x[o + 7], B, 4249261313),
            t = h(t, u, v, w, x[o + 8], y, 1770035416),
            w = h(w, t, u, v, x[o + 9], z, 2336552879),
            v = h(v, w, t, u, x[o + 10], A, 4294925233),
            u = h(u, v, w, t, x[o + 11], B, 2304563134),
            t = h(t, u, v, w, x[o + 12], y, 1804603682),
            w = h(w, t, u, v, x[o + 13], z, 4254626195),
            v = h(v, w, t, u, x[o + 14], A, 2792965006),
            u = h(u, v, w, t, x[o + 15], B, 1236535329),
            t = i(t, u, v, w, x[o + 1], C, 4129170786),
            w = i(w, t, u, v, x[o + 6], D, 3225465664),
            v = i(v, w, t, u, x[o + 11], E, 643717713),
            u = i(u, v, w, t, x[o + 0], F, 3921069994),
            t = i(t, u, v, w, x[o + 5], C, 3593408605),
            w = i(w, t, u, v, x[o + 10], D, 38016083),
            v = i(v, w, t, u, x[o + 15], E, 3634488961),
            u = i(u, v, w, t, x[o + 4], F, 3889429448),
            t = i(t, u, v, w, x[o + 9], C, 568446438),
            w = i(w, t, u, v, x[o + 14], D, 3275163606),
            v = i(v, w, t, u, x[o + 3], E, 4107603335),
            u = i(u, v, w, t, x[o + 8], F, 1163531501),
            t = i(t, u, v, w, x[o + 13], C, 2850285829),
            w = i(w, t, u, v, x[o + 2], D, 4243563512),
            v = i(v, w, t, u, x[o + 7], E, 1735328473),
            u = i(u, v, w, t, x[o + 12], F, 2368359562),
            t = j(t, u, v, w, x[o + 5], G, 4294588738),
            w = j(w, t, u, v, x[o + 8], H, 2272392833),
            v = j(v, w, t, u, x[o + 11], I, 1839030562),
            u = j(u, v, w, t, x[o + 14], J, 4259657740),
            t = j(t, u, v, w, x[o + 1], G, 2763975236),
            w = j(w, t, u, v, x[o + 4], H, 1272893353),
            v = j(v, w, t, u, x[o + 7], I, 4139469664),
            u = j(u, v, w, t, x[o + 10], J, 3200236656),
            t = j(t, u, v, w, x[o + 13], G, 681279174),
            w = j(w, t, u, v, x[o + 0], H, 3936430074),
            v = j(v, w, t, u, x[o + 3], I, 3572445317),
            u = j(u, v, w, t, x[o + 6], J, 76029189),
            t = j(t, u, v, w, x[o + 9], G, 3654602809),
            w = j(w, t, u, v, x[o + 12], H, 3873151461),
            v = j(v, w, t, u, x[o + 15], I, 530742520),
            u = j(u, v, w, t, x[o + 2], J, 3299628645),
            t = k(t, u, v, w, x[o + 0], K, 4096336452),
            w = k(w, t, u, v, x[o + 7], L, 1126891415),
            v = k(v, w, t, u, x[o + 14], M, 2878612391),
            u = k(u, v, w, t, x[o + 5], N, 4237533241),
            t = k(t, u, v, w, x[o + 12], K, 1700485571),
            w = k(w, t, u, v, x[o + 3], L, 2399980690),
            v = k(v, w, t, u, x[o + 10], M, 4293915773),
            u = k(u, v, w, t, x[o + 1], N, 2240044497),
            t = k(t, u, v, w, x[o + 8], K, 1873313359),
            w = k(w, t, u, v, x[o + 15], L, 4264355552),
            v = k(v, w, t, u, x[o + 6], M, 2734768916),
            u = k(u, v, w, t, x[o + 13], N, 1309151649),
            t = k(t, u, v, w, x[o + 4], K, 4149444226),
            w = k(w, t, u, v, x[o + 11], L, 3174756917),
            v = k(v, w, t, u, x[o + 2], M, 718787259),
            u = k(u, v, w, t, x[o + 9], N, 3951481745),
            t = c(t, p),
            u = c(u, q),
            v = c(v, r),
            w = c(w, s);
    var O = m(t) + m(u) + m(v) + m(w);
    return O.toLowerCase()
};
var _tb_api_url = function (api, params, token) {
    var cfg = _tb_query(api, params);
    var b = cfg.params
        , c = cfg.options;
    c.token = token;
    var d = "//" + (c.prefix ? c.prefix + "." : "") + (c.subDomain ? c.subDomain + "." : "") + c.mainDomain + "/h5/" + b.api.toLowerCase() + "/" + b.v.toLowerCase() + "/"
        , e = (new Date).getTime()
        , f = "waptest" === c.subDomain ? "4272" : "12574478"
        , h = _tb_sign(token + "&" + e + "&" + f + "&" + JSON.stringify(b.data))
        , i = {
        appKey: f,
        t: e,
        sign: h
    }
        , j = {
        data: JSON.stringify(b.data),
        ua: b.ua
    };
    Object.keys(b).forEach(function (a) {
        "undefined" == typeof i[a] && "undefined" == typeof j[a] && (i[a] = b[a])
    });
    i.callback = "mtopjsonp" + parseInt(Math.random() * 10 + 1, 10);
    c.getJSONP ? i.type = "jsonp" : (c.getJSON || c.postJSON) && (i.type = "originaljson");
    c.querystring = i;
    c.postdata = j;
    c.path = d;
    var query = [];
    query.push("http:");
    query.push(c.path);
    query.push("?");
    var queryStr = [];
    Object.keys(c.querystring).forEach(function (a) {
        queryStr.push(a + "=" + encodeURIComponent(c.querystring[a]));
    });
    queryStr.push("data=" + encodeURIComponent(c.postdata.data));
    query.push(queryStr.join("&"));
    return query.join("");
};