package com.acme.transport.terminal.dockdoor.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dock door exceptions by code. */
interface CappedDockDoorExceptionStore {
    fun find(code: String): CappedDockDoorException?
    fun save(item: CappedDockDoorException)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDockDoorException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDockDoorExceptionStore(
    var items: MutableMap<String, CappedDockDoorException>,
    var order: MutableList<String>,
) : CappedDockDoorExceptionStore {
    override fun find(code: String): CappedDockDoorException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDockDoorException) {
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

    override fun list(): MutableList<CappedDockDoorException> {
        val out = mutableListOf<CappedDockDoorException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDockDoorExceptionStore(): CappedDockDoorExceptionStore {
    return MemoryCappedDockDoorExceptionStore(
        mutableMapOf<String, CappedDockDoorException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDockDoorExceptionStore(
    var inner: CappedDockDoorExceptionStore,
    var cache: TtlCache<CappedDockDoorException>,
    var hits: Int,
    var misses: Int,
) : CappedDockDoorExceptionStore {
    override fun find(code: String): CappedDockDoorException? {
        val hit = (cache.get(code) as CappedDockDoorException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDockDoorException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDockDoorException> = inner.list()
}

fun newCachedCappedDockDoorExceptionStore(inner: CappedDockDoorExceptionStore, ttl: Int, clock: Clock): CappedDockDoorExceptionStore {
    return CachedCappedDockDoorExceptionStore(
        inner = inner,
        cache = newTtlCache<CappedDockDoorException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dock door exceptions. Empty fields match all. */
data class CappedDockDoorExceptionQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDockDoorException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDockDoorException>): MutableList<CappedDockDoorException> {
        val matched = filter(items) { candidate: CappedDockDoorException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDockDoorExceptions(store: CappedDockDoorExceptionStore, query: CappedDockDoorExceptionQuery): MutableList<CappedDockDoorException> {
    return query.apply(store.list())
}
