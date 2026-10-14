package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling proof of delivery preferences by code. */
interface RollingProofOfDeliveryPreferenceStore {
    fun find(code: String): RollingProofOfDeliveryPreference?
    fun save(item: RollingProofOfDeliveryPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingProofOfDeliveryPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingProofOfDeliveryPreferenceStore(
    var items: MutableMap<String, RollingProofOfDeliveryPreference>,
    var order: MutableList<String>,
) : RollingProofOfDeliveryPreferenceStore {
    override fun find(code: String): RollingProofOfDeliveryPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingProofOfDeliveryPreference) {
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

    override fun list(): MutableList<RollingProofOfDeliveryPreference> {
        val out = mutableListOf<RollingProofOfDeliveryPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingProofOfDeliveryPreferenceStore(): RollingProofOfDeliveryPreferenceStore {
    return MemoryRollingProofOfDeliveryPreferenceStore(
        mutableMapOf<String, RollingProofOfDeliveryPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingProofOfDeliveryPreferenceStore(
    var inner: RollingProofOfDeliveryPreferenceStore,
    var cache: TtlCache<RollingProofOfDeliveryPreference>,
    var hits: Int,
    var misses: Int,
) : RollingProofOfDeliveryPreferenceStore {
    override fun find(code: String): RollingProofOfDeliveryPreference? {
        val hit = (cache.get(code) as RollingProofOfDeliveryPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingProofOfDeliveryPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingProofOfDeliveryPreference> = inner.list()
}

fun newCachedRollingProofOfDeliveryPreferenceStore(inner: RollingProofOfDeliveryPreferenceStore, ttl: Int, clock: Clock): RollingProofOfDeliveryPreferenceStore {
    return CachedRollingProofOfDeliveryPreferenceStore(
        inner = inner,
        cache = newTtlCache<RollingProofOfDeliveryPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling proof of delivery preferences. Empty fields match all. */
data class RollingProofOfDeliveryPreferenceQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingProofOfDeliveryPreference): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingProofOfDeliveryPreference>): MutableList<RollingProofOfDeliveryPreference> {
        val matched = filter(items) { candidate: RollingProofOfDeliveryPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingProofOfDeliveryPreferences(store: RollingProofOfDeliveryPreferenceStore, query: RollingProofOfDeliveryPreferenceQuery): MutableList<RollingProofOfDeliveryPreference> {
    return query.apply(store.list())
}
