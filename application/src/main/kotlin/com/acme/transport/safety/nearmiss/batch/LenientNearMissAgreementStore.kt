package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient near miss agreements by code. */
interface LenientNearMissAgreementStore {
    fun find(code: String): LenientNearMissAgreement?
    fun save(item: LenientNearMissAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNearMissAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNearMissAgreementStore(
    var items: MutableMap<String, LenientNearMissAgreement>,
    var order: MutableList<String>,
) : LenientNearMissAgreementStore {
    override fun find(code: String): LenientNearMissAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNearMissAgreement) {
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

    override fun list(): MutableList<LenientNearMissAgreement> {
        val out = mutableListOf<LenientNearMissAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNearMissAgreementStore(): LenientNearMissAgreementStore {
    return MemoryLenientNearMissAgreementStore(
        mutableMapOf<String, LenientNearMissAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNearMissAgreementStore(
    var inner: LenientNearMissAgreementStore,
    var cache: TtlCache<LenientNearMissAgreement>,
    var hits: Int,
    var misses: Int,
) : LenientNearMissAgreementStore {
    override fun find(code: String): LenientNearMissAgreement? {
        val hit = (cache.get(code) as LenientNearMissAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNearMissAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNearMissAgreement> = inner.list()
}

fun newCachedLenientNearMissAgreementStore(inner: LenientNearMissAgreementStore, ttl: Int, clock: Clock): LenientNearMissAgreementStore {
    return CachedLenientNearMissAgreementStore(
        inner = inner,
        cache = newTtlCache<LenientNearMissAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient near miss agreements. Empty fields match all. */
data class LenientNearMissAgreementQuery(
    var unit: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNearMissAgreement): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientNearMissAgreement>): MutableList<LenientNearMissAgreement> {
        val matched = filter(items) { candidate: LenientNearMissAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNearMissAgreements(store: LenientNearMissAgreementStore, query: LenientNearMissAgreementQuery): MutableList<LenientNearMissAgreement> {
    return query.apply(store.list())
}
