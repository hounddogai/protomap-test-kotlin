package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered redelivery bundles by code. */
interface TieredRedeliveryBundleStore {
    fun find(code: String): TieredRedeliveryBundle?
    fun save(item: TieredRedeliveryBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRedeliveryBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRedeliveryBundleStore(
    var items: MutableMap<String, TieredRedeliveryBundle>,
    var order: MutableList<String>,
) : TieredRedeliveryBundleStore {
    override fun find(code: String): TieredRedeliveryBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRedeliveryBundle) {
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

    override fun list(): MutableList<TieredRedeliveryBundle> {
        val out = mutableListOf<TieredRedeliveryBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRedeliveryBundleStore(): TieredRedeliveryBundleStore {
    return MemoryTieredRedeliveryBundleStore(
        mutableMapOf<String, TieredRedeliveryBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRedeliveryBundleStore(
    var inner: TieredRedeliveryBundleStore,
    var cache: TtlCache<TieredRedeliveryBundle>,
    var hits: Int,
    var misses: Int,
) : TieredRedeliveryBundleStore {
    override fun find(code: String): TieredRedeliveryBundle? {
        val hit = (cache.get(code) as TieredRedeliveryBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRedeliveryBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRedeliveryBundle> = inner.list()
}

fun newCachedTieredRedeliveryBundleStore(inner: TieredRedeliveryBundleStore, ttl: Int, clock: Clock): TieredRedeliveryBundleStore {
    return CachedTieredRedeliveryBundleStore(
        inner = inner,
        cache = newTtlCache<TieredRedeliveryBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered redelivery bundles. Empty fields match all. */
data class TieredRedeliveryBundleQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRedeliveryBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRedeliveryBundle>): MutableList<TieredRedeliveryBundle> {
        val matched = filter(items) { candidate: TieredRedeliveryBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRedeliveryBundles(store: TieredRedeliveryBundleStore, query: TieredRedeliveryBundleQuery): MutableList<TieredRedeliveryBundle> {
    return query.apply(store.list())
}
