package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default ev charge groups by code. */
interface DefaultEvChargeGroupStore {
    fun find(code: String): DefaultEvChargeGroup?
    fun save(item: DefaultEvChargeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultEvChargeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultEvChargeGroupStore(
    var items: MutableMap<String, DefaultEvChargeGroup>,
    var order: MutableList<String>,
) : DefaultEvChargeGroupStore {
    override fun find(code: String): DefaultEvChargeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultEvChargeGroup) {
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

    override fun list(): MutableList<DefaultEvChargeGroup> {
        val out = mutableListOf<DefaultEvChargeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultEvChargeGroupStore(): DefaultEvChargeGroupStore {
    return MemoryDefaultEvChargeGroupStore(
        mutableMapOf<String, DefaultEvChargeGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultEvChargeGroupStore(
    var inner: DefaultEvChargeGroupStore,
    var cache: TtlCache<DefaultEvChargeGroup>,
    var hits: Int,
    var misses: Int,
) : DefaultEvChargeGroupStore {
    override fun find(code: String): DefaultEvChargeGroup? {
        val hit = (cache.get(code) as DefaultEvChargeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultEvChargeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultEvChargeGroup> = inner.list()
}

fun newCachedDefaultEvChargeGroupStore(inner: DefaultEvChargeGroupStore, ttl: Int, clock: Clock): DefaultEvChargeGroupStore {
    return CachedDefaultEvChargeGroupStore(
        inner = inner,
        cache = newTtlCache<DefaultEvChargeGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default ev charge groups. Empty fields match all. */
data class DefaultEvChargeGroupQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultEvChargeGroup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultEvChargeGroup>): MutableList<DefaultEvChargeGroup> {
        val matched = filter(items) { candidate: DefaultEvChargeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultEvChargeGroups(store: DefaultEvChargeGroupStore, query: DefaultEvChargeGroupQuery): MutableList<DefaultEvChargeGroup> {
    return query.apply(store.list())
}
