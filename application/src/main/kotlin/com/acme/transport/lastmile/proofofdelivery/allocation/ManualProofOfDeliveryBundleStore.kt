package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual proof of delivery bundles by code. */
interface ManualProofOfDeliveryBundleStore {
    fun find(code: String): ManualProofOfDeliveryBundle?
    fun save(item: ManualProofOfDeliveryBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualProofOfDeliveryBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualProofOfDeliveryBundleStore(
    var items: MutableMap<String, ManualProofOfDeliveryBundle>,
    var order: MutableList<String>,
) : ManualProofOfDeliveryBundleStore {
    override fun find(code: String): ManualProofOfDeliveryBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualProofOfDeliveryBundle) {
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

    override fun list(): MutableList<ManualProofOfDeliveryBundle> {
        val out = mutableListOf<ManualProofOfDeliveryBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualProofOfDeliveryBundleStore(): ManualProofOfDeliveryBundleStore {
    return MemoryManualProofOfDeliveryBundleStore(
        mutableMapOf<String, ManualProofOfDeliveryBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualProofOfDeliveryBundleStore(
    var inner: ManualProofOfDeliveryBundleStore,
    var cache: TtlCache<ManualProofOfDeliveryBundle>,
    var hits: Int,
    var misses: Int,
) : ManualProofOfDeliveryBundleStore {
    override fun find(code: String): ManualProofOfDeliveryBundle? {
        val hit = (cache.get(code) as ManualProofOfDeliveryBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualProofOfDeliveryBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualProofOfDeliveryBundle> = inner.list()
}

fun newCachedManualProofOfDeliveryBundleStore(inner: ManualProofOfDeliveryBundleStore, ttl: Int, clock: Clock): ManualProofOfDeliveryBundleStore {
    return CachedManualProofOfDeliveryBundleStore(
        inner = inner,
        cache = newTtlCache<ManualProofOfDeliveryBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual proof of delivery bundles. Empty fields match all. */
data class ManualProofOfDeliveryBundleQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualProofOfDeliveryBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualProofOfDeliveryBundle>): MutableList<ManualProofOfDeliveryBundle> {
        val matched = filter(items) { candidate: ManualProofOfDeliveryBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualProofOfDeliveryBundles(store: ManualProofOfDeliveryBundleStore, query: ManualProofOfDeliveryBundleQuery): MutableList<ManualProofOfDeliveryBundle> {
    return query.apply(store.list())
}
