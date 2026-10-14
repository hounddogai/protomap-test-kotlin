package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict tariff cycles by code. */
interface StrictTariffCycleStore {
    fun find(code: String): StrictTariffCycle?
    fun save(item: StrictTariffCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTariffCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTariffCycleStore(
    var items: MutableMap<String, StrictTariffCycle>,
    var order: MutableList<String>,
) : StrictTariffCycleStore {
    override fun find(code: String): StrictTariffCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTariffCycle) {
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

    override fun list(): MutableList<StrictTariffCycle> {
        val out = mutableListOf<StrictTariffCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTariffCycleStore(): StrictTariffCycleStore {
    return MemoryStrictTariffCycleStore(mutableMapOf<String, StrictTariffCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTariffCycleStore(
    var inner: StrictTariffCycleStore,
    var cache: TtlCache<StrictTariffCycle>,
    var hits: Int,
    var misses: Int,
) : StrictTariffCycleStore {
    override fun find(code: String): StrictTariffCycle? {
        val hit = (cache.get(code) as StrictTariffCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTariffCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTariffCycle> = inner.list()
}

fun newCachedStrictTariffCycleStore(inner: StrictTariffCycleStore, ttl: Int, clock: Clock): StrictTariffCycleStore {
    return CachedStrictTariffCycleStore(
        inner = inner,
        cache = newTtlCache<StrictTariffCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tariff cycles. Empty fields match all. */
data class StrictTariffCycleQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTariffCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictTariffCycle>): MutableList<StrictTariffCycle> {
        val matched = filter(items) { candidate: StrictTariffCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTariffCycles(store: StrictTariffCycleStore, query: StrictTariffCycleQuery): MutableList<StrictTariffCycle> {
    return query.apply(store.list())
}
