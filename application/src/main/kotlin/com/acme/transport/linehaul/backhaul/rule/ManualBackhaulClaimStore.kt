package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual backhaul claims by code. */
interface ManualBackhaulClaimStore {
    fun find(code: String): ManualBackhaulClaim?
    fun save(item: ManualBackhaulClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualBackhaulClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualBackhaulClaimStore(
    var items: MutableMap<String, ManualBackhaulClaim>,
    var order: MutableList<String>,
) : ManualBackhaulClaimStore {
    override fun find(code: String): ManualBackhaulClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualBackhaulClaim) {
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

    override fun list(): MutableList<ManualBackhaulClaim> {
        val out = mutableListOf<ManualBackhaulClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualBackhaulClaimStore(): ManualBackhaulClaimStore {
    return MemoryManualBackhaulClaimStore(mutableMapOf<String, ManualBackhaulClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualBackhaulClaimStore(
    var inner: ManualBackhaulClaimStore,
    var cache: TtlCache<ManualBackhaulClaim>,
    var hits: Int,
    var misses: Int,
) : ManualBackhaulClaimStore {
    override fun find(code: String): ManualBackhaulClaim? {
        val hit = (cache.get(code) as ManualBackhaulClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualBackhaulClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualBackhaulClaim> = inner.list()
}

fun newCachedManualBackhaulClaimStore(inner: ManualBackhaulClaimStore, ttl: Int, clock: Clock): ManualBackhaulClaimStore {
    return CachedManualBackhaulClaimStore(
        inner = inner,
        cache = newTtlCache<ManualBackhaulClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual backhaul claims. Empty fields match all. */
data class ManualBackhaulClaimQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualBackhaulClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ManualBackhaulClaim>): MutableList<ManualBackhaulClaim> {
        val matched = filter(items) { candidate: ManualBackhaulClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualBackhaulClaims(store: ManualBackhaulClaimStore, query: ManualBackhaulClaimQuery): MutableList<ManualBackhaulClaim> {
    return query.apply(store.list())
}
