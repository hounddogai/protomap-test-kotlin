package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled redelivery windows by code. */
interface ScheduledRedeliveryWindowStore {
    fun find(code: String): ScheduledRedeliveryWindow?
    fun save(item: ScheduledRedeliveryWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRedeliveryWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRedeliveryWindowStore(
    var items: MutableMap<String, ScheduledRedeliveryWindow>,
    var order: MutableList<String>,
) : ScheduledRedeliveryWindowStore {
    override fun find(code: String): ScheduledRedeliveryWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRedeliveryWindow) {
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

    override fun list(): MutableList<ScheduledRedeliveryWindow> {
        val out = mutableListOf<ScheduledRedeliveryWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRedeliveryWindowStore(): ScheduledRedeliveryWindowStore {
    return MemoryScheduledRedeliveryWindowStore(
        mutableMapOf<String, ScheduledRedeliveryWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRedeliveryWindowStore(
    var inner: ScheduledRedeliveryWindowStore,
    var cache: TtlCache<ScheduledRedeliveryWindow>,
    var hits: Int,
    var misses: Int,
) : ScheduledRedeliveryWindowStore {
    override fun find(code: String): ScheduledRedeliveryWindow? {
        val hit = (cache.get(code) as ScheduledRedeliveryWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRedeliveryWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRedeliveryWindow> = inner.list()
}

fun newCachedScheduledRedeliveryWindowStore(inner: ScheduledRedeliveryWindowStore, ttl: Int, clock: Clock): ScheduledRedeliveryWindowStore {
    return CachedScheduledRedeliveryWindowStore(
        inner = inner,
        cache = newTtlCache<ScheduledRedeliveryWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled redelivery windows. Empty fields match all. */
data class ScheduledRedeliveryWindowQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRedeliveryWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRedeliveryWindow>): MutableList<ScheduledRedeliveryWindow> {
        val matched = filter(items) { candidate: ScheduledRedeliveryWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRedeliveryWindows(store: ScheduledRedeliveryWindowStore, query: ScheduledRedeliveryWindowQuery): MutableList<ScheduledRedeliveryWindow> {
    return query.apply(store.list())
}
