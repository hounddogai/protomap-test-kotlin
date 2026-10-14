package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff targets by code. */
interface TariffTargetStore {
    fun find(code: String): TariffTarget?
    fun save(item: TariffTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffTargetStore(
    var items: MutableMap<String, TariffTarget>,
    var order: MutableList<String>,
) : TariffTargetStore {
    override fun find(code: String): TariffTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffTarget) {
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

    override fun list(): MutableList<TariffTarget> {
        val out = mutableListOf<TariffTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffTargetStore(): TariffTargetStore {
    return MemoryTariffTargetStore(mutableMapOf<String, TariffTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffTargetStore(
    var inner: TariffTargetStore,
    var cache: TtlCache<TariffTarget>,
    var hits: Int,
    var misses: Int,
) : TariffTargetStore {
    override fun find(code: String): TariffTarget? {
        val hit = (cache.get(code) as TariffTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffTarget> = inner.list()
}

fun newCachedTariffTargetStore(inner: TariffTargetStore, ttl: Int, clock: Clock): TariffTargetStore {
    return CachedTariffTargetStore(
        inner = inner,
        cache = newTtlCache<TariffTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff targets. Empty fields match all. */
data class TariffTargetQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffTarget>): MutableList<TariffTarget> {
        val matched = filter(items) { candidate: TariffTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffTargets(store: TariffTargetStore, query: TariffTargetQuery): MutableList<TariffTarget> {
    return query.apply(store.list())
}
