package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy backhaul windows by code. */
interface LegacyBackhaulWindowStore {
    fun find(code: String): LegacyBackhaulWindow?
    fun save(item: LegacyBackhaulWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBackhaulWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBackhaulWindowStore(
    var items: MutableMap<String, LegacyBackhaulWindow>,
    var order: MutableList<String>,
) : LegacyBackhaulWindowStore {
    override fun find(code: String): LegacyBackhaulWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBackhaulWindow) {
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

    override fun list(): MutableList<LegacyBackhaulWindow> {
        val out = mutableListOf<LegacyBackhaulWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBackhaulWindowStore(): LegacyBackhaulWindowStore {
    return MemoryLegacyBackhaulWindowStore(
        mutableMapOf<String, LegacyBackhaulWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBackhaulWindowStore(
    var inner: LegacyBackhaulWindowStore,
    var cache: TtlCache<LegacyBackhaulWindow>,
    var hits: Int,
    var misses: Int,
) : LegacyBackhaulWindowStore {
    override fun find(code: String): LegacyBackhaulWindow? {
        val hit = (cache.get(code) as LegacyBackhaulWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBackhaulWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBackhaulWindow> = inner.list()
}

fun newCachedLegacyBackhaulWindowStore(inner: LegacyBackhaulWindowStore, ttl: Int, clock: Clock): LegacyBackhaulWindowStore {
    return CachedLegacyBackhaulWindowStore(
        inner = inner,
        cache = newTtlCache<LegacyBackhaulWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy backhaul windows. Empty fields match all. */
data class LegacyBackhaulWindowQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBackhaulWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LegacyBackhaulWindow>): MutableList<LegacyBackhaulWindow> {
        val matched = filter(items) { candidate: LegacyBackhaulWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBackhaulWindows(store: LegacyBackhaulWindowStore, query: LegacyBackhaulWindowQuery): MutableList<LegacyBackhaulWindow> {
    return query.apply(store.list())
}
