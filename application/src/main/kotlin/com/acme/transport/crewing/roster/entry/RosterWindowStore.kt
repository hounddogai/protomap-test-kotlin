package com.acme.transport.crewing.roster.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster windows by code. */
interface RosterWindowStore {
    fun find(code: String): RosterWindow?
    fun save(item: RosterWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterWindowStore(
    var items: MutableMap<String, RosterWindow>,
    var order: MutableList<String>,
) : RosterWindowStore {
    override fun find(code: String): RosterWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterWindow) {
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

    override fun list(): MutableList<RosterWindow> {
        val out = mutableListOf<RosterWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterWindowStore(): RosterWindowStore {
    return MemoryRosterWindowStore(mutableMapOf<String, RosterWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterWindowStore(
    var inner: RosterWindowStore,
    var cache: TtlCache<RosterWindow>,
    var hits: Int,
    var misses: Int,
) : RosterWindowStore {
    override fun find(code: String): RosterWindow? {
        val hit = (cache.get(code) as RosterWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterWindow> = inner.list()
}

fun newCachedRosterWindowStore(inner: RosterWindowStore, ttl: Int, clock: Clock): RosterWindowStore {
    return CachedRosterWindowStore(
        inner = inner,
        cache = newTtlCache<RosterWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster windows. Empty fields match all. */
data class RosterWindowQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterWindow): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterWindow>): MutableList<RosterWindow> {
        val matched = filter(items) { candidate: RosterWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterWindows(store: RosterWindowStore, query: RosterWindowQuery): MutableList<RosterWindow> {
    return query.apply(store.list())
}
