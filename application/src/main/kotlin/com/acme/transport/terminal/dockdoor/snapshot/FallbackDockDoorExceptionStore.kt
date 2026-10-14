package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dock door exceptions by code. */
interface FallbackDockDoorExceptionStore {
    fun find(code: String): FallbackDockDoorException?
    fun save(item: FallbackDockDoorException)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDockDoorException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDockDoorExceptionStore(
    var items: MutableMap<String, FallbackDockDoorException>,
    var order: MutableList<String>,
) : FallbackDockDoorExceptionStore {
    override fun find(code: String): FallbackDockDoorException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDockDoorException) {
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

    override fun list(): MutableList<FallbackDockDoorException> {
        val out = mutableListOf<FallbackDockDoorException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDockDoorExceptionStore(): FallbackDockDoorExceptionStore {
    return MemoryFallbackDockDoorExceptionStore(
        mutableMapOf<String, FallbackDockDoorException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDockDoorExceptionStore(
    var inner: FallbackDockDoorExceptionStore,
    var cache: TtlCache<FallbackDockDoorException>,
    var hits: Int,
    var misses: Int,
) : FallbackDockDoorExceptionStore {
    override fun find(code: String): FallbackDockDoorException? {
        val hit = (cache.get(code) as FallbackDockDoorException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDockDoorException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDockDoorException> = inner.list()
}

fun newCachedFallbackDockDoorExceptionStore(inner: FallbackDockDoorExceptionStore, ttl: Int, clock: Clock): FallbackDockDoorExceptionStore {
    return CachedFallbackDockDoorExceptionStore(
        inner = inner,
        cache = newTtlCache<FallbackDockDoorException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dock door exceptions. Empty fields match all. */
data class FallbackDockDoorExceptionQuery(
    var lane: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDockDoorException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDockDoorException>): MutableList<FallbackDockDoorException> {
        val matched = filter(items) { candidate: FallbackDockDoorException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDockDoorExceptions(store: FallbackDockDoorExceptionStore, query: FallbackDockDoorExceptionQuery): MutableList<FallbackDockDoorException> {
    return query.apply(store.list())
}
