package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual relay baselines by code. */
interface ManualRelayBaselineStore {
    fun find(code: String): ManualRelayBaseline?
    fun save(item: ManualRelayBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRelayBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRelayBaselineStore(
    var items: MutableMap<String, ManualRelayBaseline>,
    var order: MutableList<String>,
) : ManualRelayBaselineStore {
    override fun find(code: String): ManualRelayBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRelayBaseline) {
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

    override fun list(): MutableList<ManualRelayBaseline> {
        val out = mutableListOf<ManualRelayBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRelayBaselineStore(): ManualRelayBaselineStore {
    return MemoryManualRelayBaselineStore(mutableMapOf<String, ManualRelayBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRelayBaselineStore(
    var inner: ManualRelayBaselineStore,
    var cache: TtlCache<ManualRelayBaseline>,
    var hits: Int,
    var misses: Int,
) : ManualRelayBaselineStore {
    override fun find(code: String): ManualRelayBaseline? {
        val hit = (cache.get(code) as ManualRelayBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRelayBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRelayBaseline> = inner.list()
}

fun newCachedManualRelayBaselineStore(inner: ManualRelayBaselineStore, ttl: Int, clock: Clock): ManualRelayBaselineStore {
    return CachedManualRelayBaselineStore(
        inner = inner,
        cache = newTtlCache<ManualRelayBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual relay baselines. Empty fields match all. */
data class ManualRelayBaselineQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRelayBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRelayBaseline>): MutableList<ManualRelayBaseline> {
        val matched = filter(items) { candidate: ManualRelayBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRelayBaselines(store: ManualRelayBaselineStore, query: ManualRelayBaselineQuery): MutableList<ManualRelayBaseline> {
    return query.apply(store.list())
}
