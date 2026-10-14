package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation agreements by code. */
interface ViolationAgreementStore {
    fun find(code: String): ViolationAgreement?
    fun save(item: ViolationAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationAgreementStore(
    var items: MutableMap<String, ViolationAgreement>,
    var order: MutableList<String>,
) : ViolationAgreementStore {
    override fun find(code: String): ViolationAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationAgreement) {
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

    override fun list(): MutableList<ViolationAgreement> {
        val out = mutableListOf<ViolationAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationAgreementStore(): ViolationAgreementStore {
    return MemoryViolationAgreementStore(mutableMapOf<String, ViolationAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationAgreementStore(
    var inner: ViolationAgreementStore,
    var cache: TtlCache<ViolationAgreement>,
    var hits: Int,
    var misses: Int,
) : ViolationAgreementStore {
    override fun find(code: String): ViolationAgreement? {
        val hit = (cache.get(code) as ViolationAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationAgreement> = inner.list()
}

fun newCachedViolationAgreementStore(inner: ViolationAgreementStore, ttl: Int, clock: Clock): ViolationAgreementStore {
    return CachedViolationAgreementStore(
        inner = inner,
        cache = newTtlCache<ViolationAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation agreements. Empty fields match all. */
data class ViolationAgreementQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ViolationAgreement>): MutableList<ViolationAgreement> {
        val matched = filter(items) { candidate: ViolationAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationAgreements(store: ViolationAgreementStore, query: ViolationAgreementQuery): MutableList<ViolationAgreement> {
    return query.apply(store.list())
}
