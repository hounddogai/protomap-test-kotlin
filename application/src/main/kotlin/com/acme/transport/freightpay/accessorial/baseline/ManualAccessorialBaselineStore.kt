package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual accessorial baselines by code. */
interface ManualAccessorialBaselineStore {
    fun find(code: String): ManualAccessorialBaseline?
    fun save(item: ManualAccessorialBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAccessorialBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAccessorialBaselineStore(
    var items: MutableMap<String, ManualAccessorialBaseline>,
    var order: MutableList<String>,
) : ManualAccessorialBaselineStore {
    override fun find(code: String): ManualAccessorialBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAccessorialBaseline) {
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

    override fun list(): MutableList<ManualAccessorialBaseline> {
        val out = mutableListOf<ManualAccessorialBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAccessorialBaselineStore(): ManualAccessorialBaselineStore {
    return MemoryManualAccessorialBaselineStore(
        mutableMapOf<String, ManualAccessorialBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAccessorialBaselineStore(
    var inner: ManualAccessorialBaselineStore,
    var cache: TtlCache<ManualAccessorialBaseline>,
    var hits: Int,
    var misses: Int,
) : ManualAccessorialBaselineStore {
    override fun find(code: String): ManualAccessorialBaseline? {
        val hit = (cache.get(code) as ManualAccessorialBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAccessorialBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAccessorialBaseline> = inner.list()
}

fun newCachedManualAccessorialBaselineStore(inner: ManualAccessorialBaselineStore, ttl: Int, clock: Clock): ManualAccessorialBaselineStore {
    return CachedManualAccessorialBaselineStore(
        inner = inner,
        cache = newTtlCache<ManualAccessorialBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual accessorial baselines. Empty fields match all. */
data class ManualAccessorialBaselineQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAccessorialBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAccessorialBaseline>): MutableList<ManualAccessorialBaseline> {
        val matched = filter(items) { candidate: ManualAccessorialBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAccessorialBaselines(store: ManualAccessorialBaselineStore, query: ManualAccessorialBaselineQuery): MutableList<ManualAccessorialBaseline> {
    return query.apply(store.list())
}
