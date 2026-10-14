package com.acme.transport.customs.hold.group

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold agreements by code. */
interface HoldAgreementStore {
    fun find(code: String): HoldAgreement?
    fun save(item: HoldAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldAgreementStore(
    var items: MutableMap<String, HoldAgreement>,
    var order: MutableList<String>,
) : HoldAgreementStore {
    override fun find(code: String): HoldAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldAgreement) {
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

    override fun list(): MutableList<HoldAgreement> {
        val out = mutableListOf<HoldAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldAgreementStore(): HoldAgreementStore {
    return MemoryHoldAgreementStore(mutableMapOf<String, HoldAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldAgreementStore(
    var inner: HoldAgreementStore,
    var cache: TtlCache<HoldAgreement>,
    var hits: Int,
    var misses: Int,
) : HoldAgreementStore {
    override fun find(code: String): HoldAgreement? {
        val hit = (cache.get(code) as HoldAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldAgreement> = inner.list()
}

fun newCachedHoldAgreementStore(inner: HoldAgreementStore, ttl: Int, clock: Clock): HoldAgreementStore {
    return CachedHoldAgreementStore(
        inner = inner,
        cache = newTtlCache<HoldAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold agreements. Empty fields match all. */
data class HoldAgreementQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldAgreement): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldAgreement>): MutableList<HoldAgreement> {
        val matched = filter(items) { candidate: HoldAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldAgreements(store: HoldAgreementStore, query: HoldAgreementQuery): MutableList<HoldAgreement> {
    return query.apply(store.list())
}
