package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door exceptions by code. */
interface DockDoorExceptionStore {
    fun find(code: String): DockDoorException?
    fun save(item: DockDoorException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorExceptionStore(
    var items: MutableMap<String, DockDoorException>,
    var order: MutableList<String>,
) : DockDoorExceptionStore {
    override fun find(code: String): DockDoorException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorException) {
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

    override fun list(): MutableList<DockDoorException> {
        val out = mutableListOf<DockDoorException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorExceptionStore(): DockDoorExceptionStore {
    return MemoryDockDoorExceptionStore(mutableMapOf<String, DockDoorException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorExceptionStore(
    var inner: DockDoorExceptionStore,
    var cache: TtlCache<DockDoorException>,
    var hits: Int,
    var misses: Int,
) : DockDoorExceptionStore {
    override fun find(code: String): DockDoorException? {
        val hit = (cache.get(code) as DockDoorException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorException> = inner.list()
}

fun newCachedDockDoorExceptionStore(inner: DockDoorExceptionStore, ttl: Int, clock: Clock): DockDoorExceptionStore {
    return CachedDockDoorExceptionStore(
        inner = inner,
        cache = newTtlCache<DockDoorException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door exceptions. Empty fields match all. */
data class DockDoorExceptionQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorException): Boolean {
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

    fun apply(items: MutableList<DockDoorException>): MutableList<DockDoorException> {
        val matched = filter(items) { candidate: DockDoorException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorExceptions(store: DockDoorExceptionStore, query: DockDoorExceptionQuery): MutableList<DockDoorException> {
    return query.apply(store.list())
}
