package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt windows by code. */
interface AttemptWindowStore {
    fun find(code: String): AttemptWindow?
    fun save(item: AttemptWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptWindowStore(
    var items: MutableMap<String, AttemptWindow>,
    var order: MutableList<String>,
) : AttemptWindowStore {
    override fun find(code: String): AttemptWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptWindow) {
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

    override fun list(): MutableList<AttemptWindow> {
        val out = mutableListOf<AttemptWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptWindowStore(): AttemptWindowStore {
    return MemoryAttemptWindowStore(mutableMapOf<String, AttemptWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptWindowStore(
    var inner: AttemptWindowStore,
    var cache: TtlCache<AttemptWindow>,
    var hits: Int,
    var misses: Int,
) : AttemptWindowStore {
    override fun find(code: String): AttemptWindow? {
        val hit = (cache.get(code) as AttemptWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptWindow> = inner.list()
}

fun newCachedAttemptWindowStore(inner: AttemptWindowStore, ttl: Int, clock: Clock): AttemptWindowStore {
    return CachedAttemptWindowStore(
        inner = inner,
        cache = newTtlCache<AttemptWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt windows. Empty fields match all. */
data class AttemptWindowQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptWindow): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptWindow>): MutableList<AttemptWindow> {
        val matched = filter(items) { candidate: AttemptWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptWindows(store: AttemptWindowStore, query: AttemptWindowQuery): MutableList<AttemptWindow> {
    return query.apply(store.list())
}
