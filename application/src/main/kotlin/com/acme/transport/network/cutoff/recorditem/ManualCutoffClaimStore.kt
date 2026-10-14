package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual cutoff claims by code. */
interface ManualCutoffClaimStore {
    fun find(code: String): ManualCutoffClaim?
    fun save(item: ManualCutoffClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCutoffClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCutoffClaimStore(
    var items: MutableMap<String, ManualCutoffClaim>,
    var order: MutableList<String>,
) : ManualCutoffClaimStore {
    override fun find(code: String): ManualCutoffClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCutoffClaim) {
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

    override fun list(): MutableList<ManualCutoffClaim> {
        val out = mutableListOf<ManualCutoffClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCutoffClaimStore(): ManualCutoffClaimStore {
    return MemoryManualCutoffClaimStore(mutableMapOf<String, ManualCutoffClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCutoffClaimStore(
    var inner: ManualCutoffClaimStore,
    var cache: TtlCache<ManualCutoffClaim>,
    var hits: Int,
    var misses: Int,
) : ManualCutoffClaimStore {
    override fun find(code: String): ManualCutoffClaim? {
        val hit = (cache.get(code) as ManualCutoffClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCutoffClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCutoffClaim> = inner.list()
}

fun newCachedManualCutoffClaimStore(inner: ManualCutoffClaimStore, ttl: Int, clock: Clock): ManualCutoffClaimStore {
    return CachedManualCutoffClaimStore(
        inner = inner,
        cache = newTtlCache<ManualCutoffClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual cutoff claims. Empty fields match all. */
data class ManualCutoffClaimQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCutoffClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCutoffClaim>): MutableList<ManualCutoffClaim> {
        val matched = filter(items) { candidate: ManualCutoffClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCutoffClaims(store: ManualCutoffClaimStore, query: ManualCutoffClaimQuery): MutableList<ManualCutoffClaim> {
    return query.apply(store.list())
}
