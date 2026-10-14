package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default redelivery bundles by code. */
interface DefaultRedeliveryBundleStore {
    fun find(code: String): DefaultRedeliveryBundle?
    fun save(item: DefaultRedeliveryBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRedeliveryBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRedeliveryBundleStore(
    var items: MutableMap<String, DefaultRedeliveryBundle>,
    var order: MutableList<String>,
) : DefaultRedeliveryBundleStore {
    override fun find(code: String): DefaultRedeliveryBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRedeliveryBundle) {
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

    override fun list(): MutableList<DefaultRedeliveryBundle> {
        val out = mutableListOf<DefaultRedeliveryBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRedeliveryBundleStore(): DefaultRedeliveryBundleStore {
    return MemoryDefaultRedeliveryBundleStore(
        mutableMapOf<String, DefaultRedeliveryBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRedeliveryBundleStore(
    var inner: DefaultRedeliveryBundleStore,
    var cache: TtlCache<DefaultRedeliveryBundle>,
    var hits: Int,
    var misses: Int,
) : DefaultRedeliveryBundleStore {
    override fun find(code: String): DefaultRedeliveryBundle? {
        val hit = (cache.get(code) as DefaultRedeliveryBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRedeliveryBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRedeliveryBundle> = inner.list()
}

fun newCachedDefaultRedeliveryBundleStore(inner: DefaultRedeliveryBundleStore, ttl: Int, clock: Clock): DefaultRedeliveryBundleStore {
    return CachedDefaultRedeliveryBundleStore(
        inner = inner,
        cache = newTtlCache<DefaultRedeliveryBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default redelivery bundles. Empty fields match all. */
data class DefaultRedeliveryBundleQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRedeliveryBundle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRedeliveryBundle>): MutableList<DefaultRedeliveryBundle> {
        val matched = filter(items) { candidate: DefaultRedeliveryBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRedeliveryBundles(store: DefaultRedeliveryBundleStore, query: DefaultRedeliveryBundleQuery): MutableList<DefaultRedeliveryBundle> {
    return query.apply(store.list())
}
