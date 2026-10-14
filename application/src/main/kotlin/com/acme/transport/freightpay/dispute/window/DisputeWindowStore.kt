package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute windows by code. */
interface DisputeWindowStore {
    fun find(code: String): DisputeWindow?
    fun save(item: DisputeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeWindowStore(
    var items: MutableMap<String, DisputeWindow>,
    var order: MutableList<String>,
) : DisputeWindowStore {
    override fun find(code: String): DisputeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeWindow) {
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

    override fun list(): MutableList<DisputeWindow> {
        val out = mutableListOf<DisputeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeWindowStore(): DisputeWindowStore {
    return MemoryDisputeWindowStore(mutableMapOf<String, DisputeWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeWindowStore(
    var inner: DisputeWindowStore,
    var cache: TtlCache<DisputeWindow>,
    var hits: Int,
    var misses: Int,
) : DisputeWindowStore {
    override fun find(code: String): DisputeWindow? {
        val hit = (cache.get(code) as DisputeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeWindow> = inner.list()
}

fun newCachedDisputeWindowStore(inner: DisputeWindowStore, ttl: Int, clock: Clock): DisputeWindowStore {
    return CachedDisputeWindowStore(
        inner = inner,
        cache = newTtlCache<DisputeWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute windows. Empty fields match all. */
data class DisputeWindowQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<DisputeWindow>): MutableList<DisputeWindow> {
        val matched = filter(items) { candidate: DisputeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeWindows(store: DisputeWindowStore, query: DisputeWindowQuery): MutableList<DisputeWindow> {
    return query.apply(store.list())
}
