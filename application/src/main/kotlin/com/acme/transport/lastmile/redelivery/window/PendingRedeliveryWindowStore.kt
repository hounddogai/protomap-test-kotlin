package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending redelivery windows by code. */
interface PendingRedeliveryWindowStore {
    fun find(code: String): PendingRedeliveryWindow?
    fun save(item: PendingRedeliveryWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRedeliveryWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRedeliveryWindowStore(
    var items: MutableMap<String, PendingRedeliveryWindow>,
    var order: MutableList<String>,
) : PendingRedeliveryWindowStore {
    override fun find(code: String): PendingRedeliveryWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRedeliveryWindow) {
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

    override fun list(): MutableList<PendingRedeliveryWindow> {
        val out = mutableListOf<PendingRedeliveryWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRedeliveryWindowStore(): PendingRedeliveryWindowStore {
    return MemoryPendingRedeliveryWindowStore(
        mutableMapOf<String, PendingRedeliveryWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRedeliveryWindowStore(
    var inner: PendingRedeliveryWindowStore,
    var cache: TtlCache<PendingRedeliveryWindow>,
    var hits: Int,
    var misses: Int,
) : PendingRedeliveryWindowStore {
    override fun find(code: String): PendingRedeliveryWindow? {
        val hit = (cache.get(code) as PendingRedeliveryWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRedeliveryWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRedeliveryWindow> = inner.list()
}

fun newCachedPendingRedeliveryWindowStore(inner: PendingRedeliveryWindowStore, ttl: Int, clock: Clock): PendingRedeliveryWindowStore {
    return CachedPendingRedeliveryWindowStore(
        inner = inner,
        cache = newTtlCache<PendingRedeliveryWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending redelivery windows. Empty fields match all. */
data class PendingRedeliveryWindowQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRedeliveryWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<PendingRedeliveryWindow>): MutableList<PendingRedeliveryWindow> {
        val matched = filter(items) { candidate: PendingRedeliveryWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRedeliveryWindows(store: PendingRedeliveryWindowStore, query: PendingRedeliveryWindowQuery): MutableList<PendingRedeliveryWindow> {
    return query.apply(store.list())
}
