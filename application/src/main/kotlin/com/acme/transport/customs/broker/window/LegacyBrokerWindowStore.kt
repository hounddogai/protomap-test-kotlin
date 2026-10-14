package com.acme.transport.customs.broker.window

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy broker windows by code. */
interface LegacyBrokerWindowStore {
    fun find(code: String): LegacyBrokerWindow?
    fun save(item: LegacyBrokerWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBrokerWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBrokerWindowStore(
    var items: MutableMap<String, LegacyBrokerWindow>,
    var order: MutableList<String>,
) : LegacyBrokerWindowStore {
    override fun find(code: String): LegacyBrokerWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBrokerWindow) {
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

    override fun list(): MutableList<LegacyBrokerWindow> {
        val out = mutableListOf<LegacyBrokerWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBrokerWindowStore(): LegacyBrokerWindowStore {
    return MemoryLegacyBrokerWindowStore(mutableMapOf<String, LegacyBrokerWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBrokerWindowStore(
    var inner: LegacyBrokerWindowStore,
    var cache: TtlCache<LegacyBrokerWindow>,
    var hits: Int,
    var misses: Int,
) : LegacyBrokerWindowStore {
    override fun find(code: String): LegacyBrokerWindow? {
        val hit = (cache.get(code) as LegacyBrokerWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBrokerWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBrokerWindow> = inner.list()
}

fun newCachedLegacyBrokerWindowStore(inner: LegacyBrokerWindowStore, ttl: Int, clock: Clock): LegacyBrokerWindowStore {
    return CachedLegacyBrokerWindowStore(
        inner = inner,
        cache = newTtlCache<LegacyBrokerWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy broker windows. Empty fields match all. */
data class LegacyBrokerWindowQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBrokerWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBrokerWindow>): MutableList<LegacyBrokerWindow> {
        val matched = filter(items) { candidate: LegacyBrokerWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBrokerWindows(store: LegacyBrokerWindowStore, query: LegacyBrokerWindowQuery): MutableList<LegacyBrokerWindow> {
    return query.apply(store.list())
}
