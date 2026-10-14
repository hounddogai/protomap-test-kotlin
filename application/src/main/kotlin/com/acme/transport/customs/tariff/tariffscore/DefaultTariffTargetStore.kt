package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default tariff targets by code. */
interface DefaultTariffTargetStore {
    fun find(code: String): DefaultTariffTarget?
    fun save(item: DefaultTariffTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTariffTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTariffTargetStore(
    var items: MutableMap<String, DefaultTariffTarget>,
    var order: MutableList<String>,
) : DefaultTariffTargetStore {
    override fun find(code: String): DefaultTariffTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTariffTarget) {
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

    override fun list(): MutableList<DefaultTariffTarget> {
        val out = mutableListOf<DefaultTariffTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTariffTargetStore(): DefaultTariffTargetStore {
    return MemoryDefaultTariffTargetStore(mutableMapOf<String, DefaultTariffTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTariffTargetStore(
    var inner: DefaultTariffTargetStore,
    var cache: TtlCache<DefaultTariffTarget>,
    var hits: Int,
    var misses: Int,
) : DefaultTariffTargetStore {
    override fun find(code: String): DefaultTariffTarget? {
        val hit = (cache.get(code) as DefaultTariffTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTariffTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTariffTarget> = inner.list()
}

fun newCachedDefaultTariffTargetStore(inner: DefaultTariffTargetStore, ttl: Int, clock: Clock): DefaultTariffTargetStore {
    return CachedDefaultTariffTargetStore(
        inner = inner,
        cache = newTtlCache<DefaultTariffTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tariff targets. Empty fields match all. */
data class DefaultTariffTargetQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTariffTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTariffTarget>): MutableList<DefaultTariffTarget> {
        val matched = filter(items) { candidate: DefaultTariffTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTariffTargets(store: DefaultTariffTargetStore, query: DefaultTariffTargetQuery): MutableList<DefaultTariffTarget> {
    return query.apply(store.list())
}
