package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rest break windows by code. */
interface DefaultRestBreakWindowStore {
    fun find(code: String): DefaultRestBreakWindow?
    fun save(item: DefaultRestBreakWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRestBreakWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRestBreakWindowStore(
    var items: MutableMap<String, DefaultRestBreakWindow>,
    var order: MutableList<String>,
) : DefaultRestBreakWindowStore {
    override fun find(code: String): DefaultRestBreakWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRestBreakWindow) {
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

    override fun list(): MutableList<DefaultRestBreakWindow> {
        val out = mutableListOf<DefaultRestBreakWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRestBreakWindowStore(): DefaultRestBreakWindowStore {
    return MemoryDefaultRestBreakWindowStore(
        mutableMapOf<String, DefaultRestBreakWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRestBreakWindowStore(
    var inner: DefaultRestBreakWindowStore,
    var cache: TtlCache<DefaultRestBreakWindow>,
    var hits: Int,
    var misses: Int,
) : DefaultRestBreakWindowStore {
    override fun find(code: String): DefaultRestBreakWindow? {
        val hit = (cache.get(code) as DefaultRestBreakWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRestBreakWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRestBreakWindow> = inner.list()
}

fun newCachedDefaultRestBreakWindowStore(inner: DefaultRestBreakWindowStore, ttl: Int, clock: Clock): DefaultRestBreakWindowStore {
    return CachedDefaultRestBreakWindowStore(
        inner = inner,
        cache = newTtlCache<DefaultRestBreakWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rest break windows. Empty fields match all. */
data class DefaultRestBreakWindowQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRestBreakWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRestBreakWindow>): MutableList<DefaultRestBreakWindow> {
        val matched = filter(items) { candidate: DefaultRestBreakWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRestBreakWindows(store: DefaultRestBreakWindowStore, query: DefaultRestBreakWindowQuery): MutableList<DefaultRestBreakWindow> {
    return query.apply(store.list())
}
