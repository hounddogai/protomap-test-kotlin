package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default proof of delivery bundles by code. */
interface DefaultProofOfDeliveryBundleStore {
    fun find(code: String): DefaultProofOfDeliveryBundle?
    fun save(item: DefaultProofOfDeliveryBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultProofOfDeliveryBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultProofOfDeliveryBundleStore(
    var items: MutableMap<String, DefaultProofOfDeliveryBundle>,
    var order: MutableList<String>,
) : DefaultProofOfDeliveryBundleStore {
    override fun find(code: String): DefaultProofOfDeliveryBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultProofOfDeliveryBundle) {
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

    override fun list(): MutableList<DefaultProofOfDeliveryBundle> {
        val out = mutableListOf<DefaultProofOfDeliveryBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultProofOfDeliveryBundleStore(): DefaultProofOfDeliveryBundleStore {
    return MemoryDefaultProofOfDeliveryBundleStore(
        mutableMapOf<String, DefaultProofOfDeliveryBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultProofOfDeliveryBundleStore(
    var inner: DefaultProofOfDeliveryBundleStore,
    var cache: TtlCache<DefaultProofOfDeliveryBundle>,
    var hits: Int,
    var misses: Int,
) : DefaultProofOfDeliveryBundleStore {
    override fun find(code: String): DefaultProofOfDeliveryBundle? {
        val hit = (cache.get(code) as DefaultProofOfDeliveryBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultProofOfDeliveryBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultProofOfDeliveryBundle> = inner.list()
}

fun newCachedDefaultProofOfDeliveryBundleStore(inner: DefaultProofOfDeliveryBundleStore, ttl: Int, clock: Clock): DefaultProofOfDeliveryBundleStore {
    return CachedDefaultProofOfDeliveryBundleStore(
        inner = inner,
        cache = newTtlCache<DefaultProofOfDeliveryBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default proof of delivery bundles. Empty fields match all. */
data class DefaultProofOfDeliveryBundleQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultProofOfDeliveryBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultProofOfDeliveryBundle>): MutableList<DefaultProofOfDeliveryBundle> {
        val matched = filter(items) { candidate: DefaultProofOfDeliveryBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultProofOfDeliveryBundles(store: DefaultProofOfDeliveryBundleStore, query: DefaultProofOfDeliveryBundleQuery): MutableList<DefaultProofOfDeliveryBundle> {
    return query.apply(store.list())
}
