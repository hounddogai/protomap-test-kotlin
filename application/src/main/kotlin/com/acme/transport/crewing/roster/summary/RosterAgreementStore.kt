package com.acme.transport.crewing.roster.summary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster agreements by code. */
interface RosterAgreementStore {
    fun find(code: String): RosterAgreement?
    fun save(item: RosterAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterAgreementStore(
    var items: MutableMap<String, RosterAgreement>,
    var order: MutableList<String>,
) : RosterAgreementStore {
    override fun find(code: String): RosterAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterAgreement) {
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

    override fun list(): MutableList<RosterAgreement> {
        val out = mutableListOf<RosterAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterAgreementStore(): RosterAgreementStore {
    return MemoryRosterAgreementStore(mutableMapOf<String, RosterAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterAgreementStore(
    var inner: RosterAgreementStore,
    var cache: TtlCache<RosterAgreement>,
    var hits: Int,
    var misses: Int,
) : RosterAgreementStore {
    override fun find(code: String): RosterAgreement? {
        val hit = (cache.get(code) as RosterAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterAgreement> = inner.list()
}

fun newCachedRosterAgreementStore(inner: RosterAgreementStore, ttl: Int, clock: Clock): RosterAgreementStore {
    return CachedRosterAgreementStore(
        inner = inner,
        cache = newTtlCache<RosterAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster agreements. Empty fields match all. */
data class RosterAgreementQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterAgreement): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<RosterAgreement>): MutableList<RosterAgreement> {
        val matched = filter(items) { candidate: RosterAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterAgreements(store: RosterAgreementStore, query: RosterAgreementQuery): MutableList<RosterAgreement> {
    return query.apply(store.list())
}
