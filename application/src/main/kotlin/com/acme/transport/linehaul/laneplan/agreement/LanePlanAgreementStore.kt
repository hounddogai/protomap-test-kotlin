package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan agreements by code. */
interface LanePlanAgreementStore {
    fun find(code: String): LanePlanAgreement?
    fun save(item: LanePlanAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanAgreementStore(
    var items: MutableMap<String, LanePlanAgreement>,
    var order: MutableList<String>,
) : LanePlanAgreementStore {
    override fun find(code: String): LanePlanAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanAgreement) {
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

    override fun list(): MutableList<LanePlanAgreement> {
        val out = mutableListOf<LanePlanAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanAgreementStore(): LanePlanAgreementStore {
    return MemoryLanePlanAgreementStore(mutableMapOf<String, LanePlanAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanAgreementStore(
    var inner: LanePlanAgreementStore,
    var cache: TtlCache<LanePlanAgreement>,
    var hits: Int,
    var misses: Int,
) : LanePlanAgreementStore {
    override fun find(code: String): LanePlanAgreement? {
        val hit = (cache.get(code) as LanePlanAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanAgreement> = inner.list()
}

fun newCachedLanePlanAgreementStore(inner: LanePlanAgreementStore, ttl: Int, clock: Clock): LanePlanAgreementStore {
    return CachedLanePlanAgreementStore(
        inner = inner,
        cache = newTtlCache<LanePlanAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan agreements. Empty fields match all. */
data class LanePlanAgreementQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanAgreement>): MutableList<LanePlanAgreement> {
        val matched = filter(items) { candidate: LanePlanAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanAgreements(store: LanePlanAgreementStore, query: LanePlanAgreementQuery): MutableList<LanePlanAgreement> {
    return query.apply(store.list())
}
