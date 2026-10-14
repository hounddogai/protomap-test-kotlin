package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rate card baselines by code. */
interface GlobalRateCardBaselineStore {
    fun find(code: String): GlobalRateCardBaseline?
    fun save(item: GlobalRateCardBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRateCardBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRateCardBaselineStore(
    var items: MutableMap<String, GlobalRateCardBaseline>,
    var order: MutableList<String>,
) : GlobalRateCardBaselineStore {
    override fun find(code: String): GlobalRateCardBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRateCardBaseline) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<GlobalRateCardBaseline> {
        val out = mutableListOf<GlobalRateCardBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRateCardBaselineStore(): GlobalRateCardBaselineStore {
    return MemoryGlobalRateCardBaselineStore(
        mutableMapOf<String, GlobalRateCardBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRateCardBaselineStore(
    var inner: GlobalRateCardBaselineStore,
    var cache: TtlCache<GlobalRateCardBaseline>,
    var hits: Int,
    var misses: Int,
) : GlobalRateCardBaselineStore {
    override fun find(code: String): GlobalRateCardBaseline? {
        val hit = (cache.get(code) as GlobalRateCardBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRateCardBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRateCardBaseline> = inner.list()
}

fun newCachedGlobalRateCardBaselineStore(inner: GlobalRateCardBaselineStore, ttl: Int, clock: Clock): GlobalRateCardBaselineStore {
    return CachedGlobalRateCardBaselineStore(
        inner = inner,
        cache = newTtlCache<GlobalRateCardBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rate card baselines. Empty fields match all. */
data class GlobalRateCardBaselineQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRateCardBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRateCardBaseline>): MutableList<GlobalRateCardBaseline> {
        val matched = filter(items) { candidate: GlobalRateCardBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRateCardBaselines(store: GlobalRateCardBaselineStore, query: GlobalRateCardBaselineQuery): MutableList<GlobalRateCardBaseline> {
    return query.apply(store.list())
}
