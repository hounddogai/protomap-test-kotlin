package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender windows by code. */
interface TenderWindowStore {
    fun find(code: String): TenderWindow?
    fun save(item: TenderWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderWindowStore(
    var items: MutableMap<String, TenderWindow>,
    var order: MutableList<String>,
) : TenderWindowStore {
    override fun find(code: String): TenderWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderWindow) {
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

    override fun list(): MutableList<TenderWindow> {
        val out = mutableListOf<TenderWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderWindowStore(): TenderWindowStore {
    return MemoryTenderWindowStore(mutableMapOf<String, TenderWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderWindowStore(
    var inner: TenderWindowStore,
    var cache: TtlCache<TenderWindow>,
    var hits: Int,
    var misses: Int,
) : TenderWindowStore {
    override fun find(code: String): TenderWindow? {
        val hit = (cache.get(code) as TenderWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderWindow> = inner.list()
}

fun newCachedTenderWindowStore(inner: TenderWindowStore, ttl: Int, clock: Clock): TenderWindowStore {
    return CachedTenderWindowStore(
        inner = inner,
        cache = newTtlCache<TenderWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender windows. Empty fields match all. */
data class TenderWindowQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderWindow): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderWindow>): MutableList<TenderWindow> {
        val matched = filter(items) { candidate: TenderWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderWindows(store: TenderWindowStore, query: TenderWindowQuery): MutableList<TenderWindow> {
    return query.apply(store.list())
}
