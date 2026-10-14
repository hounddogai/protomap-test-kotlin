package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling proof of delivery cycles by code. */
interface RollingProofOfDeliveryCycleStore {
    fun find(code: String): RollingProofOfDeliveryCycle?
    fun save(item: RollingProofOfDeliveryCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingProofOfDeliveryCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingProofOfDeliveryCycleStore(
    var items: MutableMap<String, RollingProofOfDeliveryCycle>,
    var order: MutableList<String>,
) : RollingProofOfDeliveryCycleStore {
    override fun find(code: String): RollingProofOfDeliveryCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingProofOfDeliveryCycle) {
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

    override fun list(): MutableList<RollingProofOfDeliveryCycle> {
        val out = mutableListOf<RollingProofOfDeliveryCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingProofOfDeliveryCycleStore(): RollingProofOfDeliveryCycleStore {
    return MemoryRollingProofOfDeliveryCycleStore(
        mutableMapOf<String, RollingProofOfDeliveryCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingProofOfDeliveryCycleStore(
    var inner: RollingProofOfDeliveryCycleStore,
    var cache: TtlCache<RollingProofOfDeliveryCycle>,
    var hits: Int,
    var misses: Int,
) : RollingProofOfDeliveryCycleStore {
    override fun find(code: String): RollingProofOfDeliveryCycle? {
        val hit = (cache.get(code) as RollingProofOfDeliveryCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingProofOfDeliveryCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingProofOfDeliveryCycle> = inner.list()
}

fun newCachedRollingProofOfDeliveryCycleStore(inner: RollingProofOfDeliveryCycleStore, ttl: Int, clock: Clock): RollingProofOfDeliveryCycleStore {
    return CachedRollingProofOfDeliveryCycleStore(
        inner = inner,
        cache = newTtlCache<RollingProofOfDeliveryCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling proof of delivery cycles. Empty fields match all. */
data class RollingProofOfDeliveryCycleQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingProofOfDeliveryCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingProofOfDeliveryCycle>): MutableList<RollingProofOfDeliveryCycle> {
        val matched = filter(items) { candidate: RollingProofOfDeliveryCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingProofOfDeliveryCycles(store: RollingProofOfDeliveryCycleStore, query: RollingProofOfDeliveryCycleQuery): MutableList<RollingProofOfDeliveryCycle> {
    return query.apply(store.list())
}
