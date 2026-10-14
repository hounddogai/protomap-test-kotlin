package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling windows by code. */
interface IdlingWindowStore {
    fun find(code: String): IdlingWindow?
    fun save(item: IdlingWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingWindowStore(
    var items: MutableMap<String, IdlingWindow>,
    var order: MutableList<String>,
) : IdlingWindowStore {
    override fun find(code: String): IdlingWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingWindow) {
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

    override fun list(): MutableList<IdlingWindow> {
        val out = mutableListOf<IdlingWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingWindowStore(): IdlingWindowStore {
    return MemoryIdlingWindowStore(mutableMapOf<String, IdlingWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingWindowStore(
    var inner: IdlingWindowStore,
    var cache: TtlCache<IdlingWindow>,
    var hits: Int,
    var misses: Int,
) : IdlingWindowStore {
    override fun find(code: String): IdlingWindow? {
        val hit = (cache.get(code) as IdlingWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingWindow> = inner.list()
}

fun newCachedIdlingWindowStore(inner: IdlingWindowStore, ttl: Int, clock: Clock): IdlingWindowStore {
    return CachedIdlingWindowStore(
        inner = inner,
        cache = newTtlCache<IdlingWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling windows. Empty fields match all. */
data class IdlingWindowQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingWindow>): MutableList<IdlingWindow> {
        val matched = filter(items) { candidate: IdlingWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingWindows(store: IdlingWindowStore, query: IdlingWindowQuery): MutableList<IdlingWindow> {
    return query.apply(store.list())
}
