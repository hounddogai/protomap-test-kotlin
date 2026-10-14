package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped tender windows by code. */
interface CappedTenderWindowStore {
    fun find(code: String): CappedTenderWindow?
    fun save(item: CappedTenderWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTenderWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTenderWindowStore(
    var items: MutableMap<String, CappedTenderWindow>,
    var order: MutableList<String>,
) : CappedTenderWindowStore {
    override fun find(code: String): CappedTenderWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTenderWindow) {
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

    override fun list(): MutableList<CappedTenderWindow> {
        val out = mutableListOf<CappedTenderWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTenderWindowStore(): CappedTenderWindowStore {
    return MemoryCappedTenderWindowStore(mutableMapOf<String, CappedTenderWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTenderWindowStore(
    var inner: CappedTenderWindowStore,
    var cache: TtlCache<CappedTenderWindow>,
    var hits: Int,
    var misses: Int,
) : CappedTenderWindowStore {
    override fun find(code: String): CappedTenderWindow? {
        val hit = (cache.get(code) as CappedTenderWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTenderWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTenderWindow> = inner.list()
}

fun newCachedCappedTenderWindowStore(inner: CappedTenderWindowStore, ttl: Int, clock: Clock): CappedTenderWindowStore {
    return CachedCappedTenderWindowStore(
        inner = inner,
        cache = newTtlCache<CappedTenderWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tender windows. Empty fields match all. */
data class CappedTenderWindowQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTenderWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTenderWindow>): MutableList<CappedTenderWindow> {
        val matched = filter(items) { candidate: CappedTenderWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTenderWindows(store: CappedTenderWindowStore, query: CappedTenderWindowQuery): MutableList<CappedTenderWindow> {
    return query.apply(store.list())
}
