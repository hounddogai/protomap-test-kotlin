package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel groups by code. */
interface DefaultParcelGroupStore {
    fun find(code: String): DefaultParcelGroup?
    fun save(item: DefaultParcelGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelGroupStore(
    var items: MutableMap<String, DefaultParcelGroup>,
    var order: MutableList<String>,
) : DefaultParcelGroupStore {
    override fun find(code: String): DefaultParcelGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelGroup) {
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

    override fun list(): MutableList<DefaultParcelGroup> {
        val out = mutableListOf<DefaultParcelGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelGroupStore(): DefaultParcelGroupStore {
    return MemoryDefaultParcelGroupStore(mutableMapOf<String, DefaultParcelGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelGroupStore(
    var inner: DefaultParcelGroupStore,
    var cache: TtlCache<DefaultParcelGroup>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelGroupStore {
    override fun find(code: String): DefaultParcelGroup? {
        val hit = (cache.get(code) as DefaultParcelGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelGroup> = inner.list()
}

fun newCachedDefaultParcelGroupStore(inner: DefaultParcelGroupStore, ttl: Int, clock: Clock): DefaultParcelGroupStore {
    return CachedDefaultParcelGroupStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel groups. Empty fields match all. */
data class DefaultParcelGroupQuery(
    var region: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelGroup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultParcelGroup>): MutableList<DefaultParcelGroup> {
        val matched = filter(items) { candidate: DefaultParcelGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelGroups(store: DefaultParcelGroupStore, query: DefaultParcelGroupQuery): MutableList<DefaultParcelGroup> {
    return query.apply(store.list())
}
