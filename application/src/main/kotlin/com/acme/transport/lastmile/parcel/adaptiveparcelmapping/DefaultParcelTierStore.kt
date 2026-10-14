package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel tiers by code. */
interface DefaultParcelTierStore {
    fun find(code: String): DefaultParcelTier?
    fun save(item: DefaultParcelTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelTierStore(
    var items: MutableMap<String, DefaultParcelTier>,
    var order: MutableList<String>,
) : DefaultParcelTierStore {
    override fun find(code: String): DefaultParcelTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelTier) {
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

    override fun list(): MutableList<DefaultParcelTier> {
        val out = mutableListOf<DefaultParcelTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelTierStore(): DefaultParcelTierStore {
    return MemoryDefaultParcelTierStore(mutableMapOf<String, DefaultParcelTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelTierStore(
    var inner: DefaultParcelTierStore,
    var cache: TtlCache<DefaultParcelTier>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelTierStore {
    override fun find(code: String): DefaultParcelTier? {
        val hit = (cache.get(code) as DefaultParcelTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelTier> = inner.list()
}

fun newCachedDefaultParcelTierStore(inner: DefaultParcelTierStore, ttl: Int, clock: Clock): DefaultParcelTierStore {
    return CachedDefaultParcelTierStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel tiers. Empty fields match all. */
data class DefaultParcelTierQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultParcelTier>): MutableList<DefaultParcelTier> {
        val matched = filter(items) { candidate: DefaultParcelTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelTiers(store: DefaultParcelTierStore, query: DefaultParcelTierQuery): MutableList<DefaultParcelTier> {
    return query.apply(store.list())
}
