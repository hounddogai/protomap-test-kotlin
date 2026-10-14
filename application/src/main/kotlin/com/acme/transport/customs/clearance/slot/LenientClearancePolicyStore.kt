package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient clearance policies by code. */
interface LenientClearancePolicyStore {
    fun find(code: String): LenientClearancePolicy?
    fun save(item: LenientClearancePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientClearancePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientClearancePolicyStore(
    var items: MutableMap<String, LenientClearancePolicy>,
    var order: MutableList<String>,
) : LenientClearancePolicyStore {
    override fun find(code: String): LenientClearancePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientClearancePolicy) {
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

    override fun list(): MutableList<LenientClearancePolicy> {
        val out = mutableListOf<LenientClearancePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientClearancePolicyStore(): LenientClearancePolicyStore {
    return MemoryLenientClearancePolicyStore(
        mutableMapOf<String, LenientClearancePolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientClearancePolicyStore(
    var inner: LenientClearancePolicyStore,
    var cache: TtlCache<LenientClearancePolicy>,
    var hits: Int,
    var misses: Int,
) : LenientClearancePolicyStore {
    override fun find(code: String): LenientClearancePolicy? {
        val hit = (cache.get(code) as LenientClearancePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientClearancePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientClearancePolicy> = inner.list()
}

fun newCachedLenientClearancePolicyStore(inner: LenientClearancePolicyStore, ttl: Int, clock: Clock): LenientClearancePolicyStore {
    return CachedLenientClearancePolicyStore(
        inner = inner,
        cache = newTtlCache<LenientClearancePolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient clearance policies. Empty fields match all. */
data class LenientClearancePolicyQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientClearancePolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientClearancePolicy>): MutableList<LenientClearancePolicy> {
        val matched = filter(items) { candidate: LenientClearancePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientClearancePolicies(store: LenientClearancePolicyStore, query: LenientClearancePolicyQuery): MutableList<LenientClearancePolicy> {
    return query.apply(store.list())
}
