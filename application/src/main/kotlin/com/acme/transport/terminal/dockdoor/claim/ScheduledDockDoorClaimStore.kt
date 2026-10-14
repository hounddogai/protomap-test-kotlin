package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dock door claims by code. */
interface ScheduledDockDoorClaimStore {
    fun find(code: String): ScheduledDockDoorClaim?
    fun save(item: ScheduledDockDoorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDockDoorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDockDoorClaimStore(
    var items: MutableMap<String, ScheduledDockDoorClaim>,
    var order: MutableList<String>,
) : ScheduledDockDoorClaimStore {
    override fun find(code: String): ScheduledDockDoorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDockDoorClaim) {
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

    override fun list(): MutableList<ScheduledDockDoorClaim> {
        val out = mutableListOf<ScheduledDockDoorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDockDoorClaimStore(): ScheduledDockDoorClaimStore {
    return MemoryScheduledDockDoorClaimStore(
        mutableMapOf<String, ScheduledDockDoorClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDockDoorClaimStore(
    var inner: ScheduledDockDoorClaimStore,
    var cache: TtlCache<ScheduledDockDoorClaim>,
    var hits: Int,
    var misses: Int,
) : ScheduledDockDoorClaimStore {
    override fun find(code: String): ScheduledDockDoorClaim? {
        val hit = (cache.get(code) as ScheduledDockDoorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDockDoorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDockDoorClaim> = inner.list()
}

fun newCachedScheduledDockDoorClaimStore(inner: ScheduledDockDoorClaimStore, ttl: Int, clock: Clock): ScheduledDockDoorClaimStore {
    return CachedScheduledDockDoorClaimStore(
        inner = inner,
        cache = newTtlCache<ScheduledDockDoorClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dock door claims. Empty fields match all. */
data class ScheduledDockDoorClaimQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDockDoorClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<ScheduledDockDoorClaim>): MutableList<ScheduledDockDoorClaim> {
        val matched = filter(items) { candidate: ScheduledDockDoorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDockDoorClaims(store: ScheduledDockDoorClaimStore, query: ScheduledDockDoorClaimQuery): MutableList<ScheduledDockDoorClaim> {
    return query.apply(store.list())
}
