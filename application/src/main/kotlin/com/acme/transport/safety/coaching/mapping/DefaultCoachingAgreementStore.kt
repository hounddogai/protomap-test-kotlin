package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default coaching agreements by code. */
interface DefaultCoachingAgreementStore {
    fun find(code: String): DefaultCoachingAgreement?
    fun save(item: DefaultCoachingAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCoachingAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCoachingAgreementStore(
    var items: MutableMap<String, DefaultCoachingAgreement>,
    var order: MutableList<String>,
) : DefaultCoachingAgreementStore {
    override fun find(code: String): DefaultCoachingAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCoachingAgreement) {
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

    override fun list(): MutableList<DefaultCoachingAgreement> {
        val out = mutableListOf<DefaultCoachingAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCoachingAgreementStore(): DefaultCoachingAgreementStore {
    return MemoryDefaultCoachingAgreementStore(
        mutableMapOf<String, DefaultCoachingAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCoachingAgreementStore(
    var inner: DefaultCoachingAgreementStore,
    var cache: TtlCache<DefaultCoachingAgreement>,
    var hits: Int,
    var misses: Int,
) : DefaultCoachingAgreementStore {
    override fun find(code: String): DefaultCoachingAgreement? {
        val hit = (cache.get(code) as DefaultCoachingAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCoachingAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCoachingAgreement> = inner.list()
}

fun newCachedDefaultCoachingAgreementStore(inner: DefaultCoachingAgreementStore, ttl: Int, clock: Clock): DefaultCoachingAgreementStore {
    return CachedDefaultCoachingAgreementStore(
        inner = inner,
        cache = newTtlCache<DefaultCoachingAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default coaching agreements. Empty fields match all. */
data class DefaultCoachingAgreementQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCoachingAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCoachingAgreement>): MutableList<DefaultCoachingAgreement> {
        val matched = filter(items) { candidate: DefaultCoachingAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCoachingAgreements(store: DefaultCoachingAgreementStore, query: DefaultCoachingAgreementQuery): MutableList<DefaultCoachingAgreement> {
    return query.apply(store.list())
}
