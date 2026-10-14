package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell claims by code. */
interface DwellClaimStore {
    fun find(code: String): DwellClaim?
    fun save(item: DwellClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellClaimStore(
    var items: MutableMap<String, DwellClaim>,
    var order: MutableList<String>,
) : DwellClaimStore {
    override fun find(code: String): DwellClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellClaim) {
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

    override fun list(): MutableList<DwellClaim> {
        val out = mutableListOf<DwellClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellClaimStore(): DwellClaimStore {
    return MemoryDwellClaimStore(mutableMapOf<String, DwellClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellClaimStore(
    var inner: DwellClaimStore,
    var cache: TtlCache<DwellClaim>,
    var hits: Int,
    var misses: Int,
) : DwellClaimStore {
    override fun find(code: String): DwellClaim? {
        val hit = (cache.get(code) as DwellClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellClaim> = inner.list()
}

fun newCachedDwellClaimStore(inner: DwellClaimStore, ttl: Int, clock: Clock): DwellClaimStore {
    return CachedDwellClaimStore(
        inner = inner,
        cache = newTtlCache<DwellClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell claims. Empty fields match all. */
data class DwellClaimQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellClaim>): MutableList<DwellClaim> {
        val matched = filter(items) { candidate: DwellClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellClaims(store: DwellClaimStore, query: DwellClaimQuery): MutableList<DwellClaim> {
    return query.apply(store.list())
}
