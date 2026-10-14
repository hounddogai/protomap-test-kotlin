package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global capacity agreements by code. */
interface GlobalCapacityAgreementStore {
    fun find(code: String): GlobalCapacityAgreement?
    fun save(item: GlobalCapacityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCapacityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCapacityAgreementStore(
    var items: MutableMap<String, GlobalCapacityAgreement>,
    var order: MutableList<String>,
) : GlobalCapacityAgreementStore {
    override fun find(code: String): GlobalCapacityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCapacityAgreement) {
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

    override fun list(): MutableList<GlobalCapacityAgreement> {
        val out = mutableListOf<GlobalCapacityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCapacityAgreementStore(): GlobalCapacityAgreementStore {
    return MemoryGlobalCapacityAgreementStore(
        mutableMapOf<String, GlobalCapacityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCapacityAgreementStore(
    var inner: GlobalCapacityAgreementStore,
    var cache: TtlCache<GlobalCapacityAgreement>,
    var hits: Int,
    var misses: Int,
) : GlobalCapacityAgreementStore {
    override fun find(code: String): GlobalCapacityAgreement? {
        val hit = (cache.get(code) as GlobalCapacityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCapacityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCapacityAgreement> = inner.list()
}

fun newCachedGlobalCapacityAgreementStore(inner: GlobalCapacityAgreementStore, ttl: Int, clock: Clock): GlobalCapacityAgreementStore {
    return CachedGlobalCapacityAgreementStore(
        inner = inner,
        cache = newTtlCache<GlobalCapacityAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global capacity agreements. Empty fields match all. */
data class GlobalCapacityAgreementQuery(
    var depot: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCapacityAgreement): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<GlobalCapacityAgreement>): MutableList<GlobalCapacityAgreement> {
        val matched = filter(items) { candidate: GlobalCapacityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCapacityAgreements(store: GlobalCapacityAgreementStore, query: GlobalCapacityAgreementQuery): MutableList<GlobalCapacityAgreement> {
    return query.apply(store.list())
}
