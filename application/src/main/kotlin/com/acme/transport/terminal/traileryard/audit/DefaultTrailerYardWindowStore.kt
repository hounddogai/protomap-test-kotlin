package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default trailer yard windows by code. */
interface DefaultTrailerYardWindowStore {
    fun find(code: String): DefaultTrailerYardWindow?
    fun save(item: DefaultTrailerYardWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTrailerYardWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTrailerYardWindowStore(
    var items: MutableMap<String, DefaultTrailerYardWindow>,
    var order: MutableList<String>,
) : DefaultTrailerYardWindowStore {
    override fun find(code: String): DefaultTrailerYardWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTrailerYardWindow) {
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

    override fun list(): MutableList<DefaultTrailerYardWindow> {
        val out = mutableListOf<DefaultTrailerYardWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTrailerYardWindowStore(): DefaultTrailerYardWindowStore {
    return MemoryDefaultTrailerYardWindowStore(
        mutableMapOf<String, DefaultTrailerYardWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTrailerYardWindowStore(
    var inner: DefaultTrailerYardWindowStore,
    var cache: TtlCache<DefaultTrailerYardWindow>,
    var hits: Int,
    var misses: Int,
) : DefaultTrailerYardWindowStore {
    override fun find(code: String): DefaultTrailerYardWindow? {
        val hit = (cache.get(code) as DefaultTrailerYardWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTrailerYardWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTrailerYardWindow> = inner.list()
}

fun newCachedDefaultTrailerYardWindowStore(inner: DefaultTrailerYardWindowStore, ttl: Int, clock: Clock): DefaultTrailerYardWindowStore {
    return CachedDefaultTrailerYardWindowStore(
        inner = inner,
        cache = newTtlCache<DefaultTrailerYardWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default trailer yard windows. Empty fields match all. */
data class DefaultTrailerYardWindowQuery(
    var source: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTrailerYardWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTrailerYardWindow>): MutableList<DefaultTrailerYardWindow> {
        val matched = filter(items) { candidate: DefaultTrailerYardWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTrailerYardWindows(store: DefaultTrailerYardWindowStore, query: DefaultTrailerYardWindowQuery): MutableList<DefaultTrailerYardWindow> {
    return query.apply(store.list())
}
