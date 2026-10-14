package com.acme.transport.safety.violation.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global violation agreements by code. */
interface GlobalViolationAgreementStore {
    fun find(code: String): GlobalViolationAgreement?
    fun save(item: GlobalViolationAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalViolationAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalViolationAgreementStore(
    var items: MutableMap<String, GlobalViolationAgreement>,
    var order: MutableList<String>,
) : GlobalViolationAgreementStore {
    override fun find(code: String): GlobalViolationAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalViolationAgreement) {
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

    override fun list(): MutableList<GlobalViolationAgreement> {
        val out = mutableListOf<GlobalViolationAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalViolationAgreementStore(): GlobalViolationAgreementStore {
    return MemoryGlobalViolationAgreementStore(
        mutableMapOf<String, GlobalViolationAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalViolationAgreementStore(
    var inner: GlobalViolationAgreementStore,
    var cache: TtlCache<GlobalViolationAgreement>,
    var hits: Int,
    var misses: Int,
) : GlobalViolationAgreementStore {
    override fun find(code: String): GlobalViolationAgreement? {
        val hit = (cache.get(code) as GlobalViolationAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalViolationAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalViolationAgreement> = inner.list()
}

fun newCachedGlobalViolationAgreementStore(inner: GlobalViolationAgreementStore, ttl: Int, clock: Clock): GlobalViolationAgreementStore {
    return CachedGlobalViolationAgreementStore(
        inner = inner,
        cache = newTtlCache<GlobalViolationAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global violation agreements. Empty fields match all. */
data class GlobalViolationAgreementQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalViolationAgreement): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalViolationAgreement>): MutableList<GlobalViolationAgreement> {
        val matched = filter(items) { candidate: GlobalViolationAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalViolationAgreements(store: GlobalViolationAgreementStore, query: GlobalViolationAgreementQuery): MutableList<GlobalViolationAgreement> {
    return query.apply(store.list())
}
