package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery windows by code. */
interface RedeliveryWindowStore {
    fun find(code: String): RedeliveryWindow?
    fun save(item: RedeliveryWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryWindowStore(
    var items: MutableMap<String, RedeliveryWindow>,
    var order: MutableList<String>,
) : RedeliveryWindowStore {
    override fun find(code: String): RedeliveryWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryWindow) {
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

    override fun list(): MutableList<RedeliveryWindow> {
        val out = mutableListOf<RedeliveryWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryWindowStore(): RedeliveryWindowStore {
    return MemoryRedeliveryWindowStore(mutableMapOf<String, RedeliveryWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryWindowStore(
    var inner: RedeliveryWindowStore,
    var cache: TtlCache<RedeliveryWindow>,
    var hits: Int,
    var misses: Int,
) : RedeliveryWindowStore {
    override fun find(code: String): RedeliveryWindow? {
        val hit = (cache.get(code) as RedeliveryWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryWindow> = inner.list()
}

fun newCachedRedeliveryWindowStore(inner: RedeliveryWindowStore, ttl: Int, clock: Clock): RedeliveryWindowStore {
    return CachedRedeliveryWindowStore(
        inner = inner,
        cache = newTtlCache<RedeliveryWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery windows. Empty fields match all. */
data class RedeliveryWindowQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryWindow>): MutableList<RedeliveryWindow> {
        val matched = filter(items) { candidate: RedeliveryWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryWindows(store: RedeliveryWindowStore, query: RedeliveryWindowQuery): MutableList<RedeliveryWindow> {
    return query.apply(store.list())
}
