package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss agreements by code. */
interface NearMissAgreementStore {
    fun find(code: String): NearMissAgreement?
    fun save(item: NearMissAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissAgreementStore(
    var items: MutableMap<String, NearMissAgreement>,
    var order: MutableList<String>,
) : NearMissAgreementStore {
    override fun find(code: String): NearMissAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissAgreement) {
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

    override fun list(): MutableList<NearMissAgreement> {
        val out = mutableListOf<NearMissAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissAgreementStore(): NearMissAgreementStore {
    return MemoryNearMissAgreementStore(mutableMapOf<String, NearMissAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissAgreementStore(
    var inner: NearMissAgreementStore,
    var cache: TtlCache<NearMissAgreement>,
    var hits: Int,
    var misses: Int,
) : NearMissAgreementStore {
    override fun find(code: String): NearMissAgreement? {
        val hit = (cache.get(code) as NearMissAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissAgreement> = inner.list()
}

fun newCachedNearMissAgreementStore(inner: NearMissAgreementStore, ttl: Int, clock: Clock): NearMissAgreementStore {
    return CachedNearMissAgreementStore(
        inner = inner,
        cache = newTtlCache<NearMissAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss agreements. Empty fields match all. */
data class NearMissAgreementQuery(
    var lane: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissAgreement>): MutableList<NearMissAgreement> {
        val matched = filter(items) { candidate: NearMissAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissAgreements(store: NearMissAgreementStore, query: NearMissAgreementQuery): MutableList<NearMissAgreement> {
    return query.apply(store.list())
}
