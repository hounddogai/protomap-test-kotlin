package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door claims by code. */
interface DockDoorClaimStore {
    fun find(code: String): DockDoorClaim?
    fun save(item: DockDoorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorClaimStore(
    var items: MutableMap<String, DockDoorClaim>,
    var order: MutableList<String>,
) : DockDoorClaimStore {
    override fun find(code: String): DockDoorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorClaim) {
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

    override fun list(): MutableList<DockDoorClaim> {
        val out = mutableListOf<DockDoorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorClaimStore(): DockDoorClaimStore {
    return MemoryDockDoorClaimStore(mutableMapOf<String, DockDoorClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorClaimStore(
    var inner: DockDoorClaimStore,
    var cache: TtlCache<DockDoorClaim>,
    var hits: Int,
    var misses: Int,
) : DockDoorClaimStore {
    override fun find(code: String): DockDoorClaim? {
        val hit = (cache.get(code) as DockDoorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorClaim> = inner.list()
}

fun newCachedDockDoorClaimStore(inner: DockDoorClaimStore, ttl: Int, clock: Clock): DockDoorClaimStore {
    return CachedDockDoorClaimStore(
        inner = inner,
        cache = newTtlCache<DockDoorClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door claims. Empty fields match all. */
data class DockDoorClaimQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorClaim>): MutableList<DockDoorClaim> {
        val matched = filter(items) { candidate: DockDoorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorClaims(store: DockDoorClaimStore, query: DockDoorClaimQuery): MutableList<DockDoorClaim> {
    return query.apply(store.list())
}
