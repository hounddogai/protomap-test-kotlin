package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling proof of delivery drafts by code. */
interface RollingProofOfDeliveryDraftStore {
    fun find(code: String): RollingProofOfDeliveryDraft?
    fun save(item: RollingProofOfDeliveryDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingProofOfDeliveryDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingProofOfDeliveryDraftStore(
    var items: MutableMap<String, RollingProofOfDeliveryDraft>,
    var order: MutableList<String>,
) : RollingProofOfDeliveryDraftStore {
    override fun find(code: String): RollingProofOfDeliveryDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingProofOfDeliveryDraft) {
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

    override fun list(): MutableList<RollingProofOfDeliveryDraft> {
        val out = mutableListOf<RollingProofOfDeliveryDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingProofOfDeliveryDraftStore(): RollingProofOfDeliveryDraftStore {
    return MemoryRollingProofOfDeliveryDraftStore(
        mutableMapOf<String, RollingProofOfDeliveryDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingProofOfDeliveryDraftStore(
    var inner: RollingProofOfDeliveryDraftStore,
    var cache: TtlCache<RollingProofOfDeliveryDraft>,
    var hits: Int,
    var misses: Int,
) : RollingProofOfDeliveryDraftStore {
    override fun find(code: String): RollingProofOfDeliveryDraft? {
        val hit = (cache.get(code) as RollingProofOfDeliveryDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingProofOfDeliveryDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingProofOfDeliveryDraft> = inner.list()
}

fun newCachedRollingProofOfDeliveryDraftStore(inner: RollingProofOfDeliveryDraftStore, ttl: Int, clock: Clock): RollingProofOfDeliveryDraftStore {
    return CachedRollingProofOfDeliveryDraftStore(
        inner = inner,
        cache = newTtlCache<RollingProofOfDeliveryDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling proof of delivery drafts. Empty fields match all. */
data class RollingProofOfDeliveryDraftQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingProofOfDeliveryDraft): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingProofOfDeliveryDraft>): MutableList<RollingProofOfDeliveryDraft> {
        val matched = filter(items) { candidate: RollingProofOfDeliveryDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingProofOfDeliveryDrafts(store: RollingProofOfDeliveryDraftStore, query: RollingProofOfDeliveryDraftQuery): MutableList<RollingProofOfDeliveryDraft> {
    return query.apply(store.list())
}
