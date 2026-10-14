package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident agreements by code. */
interface IncidentAgreementStore {
    fun find(code: String): IncidentAgreement?
    fun save(item: IncidentAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentAgreementStore(
    var items: MutableMap<String, IncidentAgreement>,
    var order: MutableList<String>,
) : IncidentAgreementStore {
    override fun find(code: String): IncidentAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentAgreement) {
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

    override fun list(): MutableList<IncidentAgreement> {
        val out = mutableListOf<IncidentAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentAgreementStore(): IncidentAgreementStore {
    return MemoryIncidentAgreementStore(mutableMapOf<String, IncidentAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentAgreementStore(
    var inner: IncidentAgreementStore,
    var cache: TtlCache<IncidentAgreement>,
    var hits: Int,
    var misses: Int,
) : IncidentAgreementStore {
    override fun find(code: String): IncidentAgreement? {
        val hit = (cache.get(code) as IncidentAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentAgreement> = inner.list()
}

fun newCachedIncidentAgreementStore(inner: IncidentAgreementStore, ttl: Int, clock: Clock): IncidentAgreementStore {
    return CachedIncidentAgreementStore(
        inner = inner,
        cache = newTtlCache<IncidentAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident agreements. Empty fields match all. */
data class IncidentAgreementQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentAgreement>): MutableList<IncidentAgreement> {
        val matched = filter(items) { candidate: IncidentAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentAgreements(store: IncidentAgreementStore, query: IncidentAgreementQuery): MutableList<IncidentAgreement> {
    return query.apply(store.list())
}
