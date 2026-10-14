package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped backhaul windows by code. */
interface CappedBackhaulWindowStore {
    fun find(code: String): CappedBackhaulWindow?
    fun save(item: CappedBackhaulWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBackhaulWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBackhaulWindowStore(
    var items: MutableMap<String, CappedBackhaulWindow>,
    var order: MutableList<String>,
) : CappedBackhaulWindowStore {
    override fun find(code: String): CappedBackhaulWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBackhaulWindow) {
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

    override fun list(): MutableList<CappedBackhaulWindow> {
        val out = mutableListOf<CappedBackhaulWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBackhaulWindowStore(): CappedBackhaulWindowStore {
    return MemoryCappedBackhaulWindowStore(
        mutableMapOf<String, CappedBackhaulWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBackhaulWindowStore(
    var inner: CappedBackhaulWindowStore,
    var cache: TtlCache<CappedBackhaulWindow>,
    var hits: Int,
    var misses: Int,
) : CappedBackhaulWindowStore {
    override fun find(code: String): CappedBackhaulWindow? {
        val hit = (cache.get(code) as CappedBackhaulWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBackhaulWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBackhaulWindow> = inner.list()
}

fun newCachedCappedBackhaulWindowStore(inner: CappedBackhaulWindowStore, ttl: Int, clock: Clock): CappedBackhaulWindowStore {
    return CachedCappedBackhaulWindowStore(
        inner = inner,
        cache = newTtlCache<CappedBackhaulWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped backhaul windows. Empty fields match all. */
data class CappedBackhaulWindowQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBackhaulWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedBackhaulWindow>): MutableList<CappedBackhaulWindow> {
        val matched = filter(items) { candidate: CappedBackhaulWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBackhaulWindows(store: CappedBackhaulWindowStore, query: CappedBackhaulWindowQuery): MutableList<CappedBackhaulWindow> {
    return query.apply(store.list())
}
