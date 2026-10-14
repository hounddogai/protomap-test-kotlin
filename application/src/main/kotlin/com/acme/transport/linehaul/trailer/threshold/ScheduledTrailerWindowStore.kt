package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled trailer windows by code. */
interface ScheduledTrailerWindowStore {
    fun find(code: String): ScheduledTrailerWindow?
    fun save(item: ScheduledTrailerWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerWindowStore(
    var items: MutableMap<String, ScheduledTrailerWindow>,
    var order: MutableList<String>,
) : ScheduledTrailerWindowStore {
    override fun find(code: String): ScheduledTrailerWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerWindow) {
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

    override fun list(): MutableList<ScheduledTrailerWindow> {
        val out = mutableListOf<ScheduledTrailerWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerWindowStore(): ScheduledTrailerWindowStore {
    return MemoryScheduledTrailerWindowStore(
        mutableMapOf<String, ScheduledTrailerWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerWindowStore(
    var inner: ScheduledTrailerWindowStore,
    var cache: TtlCache<ScheduledTrailerWindow>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerWindowStore {
    override fun find(code: String): ScheduledTrailerWindow? {
        val hit = (cache.get(code) as ScheduledTrailerWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerWindow> = inner.list()
}

fun newCachedScheduledTrailerWindowStore(inner: ScheduledTrailerWindowStore, ttl: Int, clock: Clock): ScheduledTrailerWindowStore {
    return CachedScheduledTrailerWindowStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer windows. Empty fields match all. */
data class ScheduledTrailerWindowQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerWindow): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTrailerWindow>): MutableList<ScheduledTrailerWindow> {
        val matched = filter(items) { candidate: ScheduledTrailerWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerWindows(store: ScheduledTrailerWindowStore, query: ScheduledTrailerWindowQuery): MutableList<ScheduledTrailerWindow> {
    return query.apply(store.list())
}
