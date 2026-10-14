package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery drafts by code. */
interface ProofOfDeliveryDraftStore {
    fun find(code: String): ProofOfDeliveryDraft?
    fun save(item: ProofOfDeliveryDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryDraftStore(
    var items: MutableMap<String, ProofOfDeliveryDraft>,
    var order: MutableList<String>,
) : ProofOfDeliveryDraftStore {
    override fun find(code: String): ProofOfDeliveryDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryDraft) {
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

    override fun list(): MutableList<ProofOfDeliveryDraft> {
        val out = mutableListOf<ProofOfDeliveryDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryDraftStore(): ProofOfDeliveryDraftStore {
    return MemoryProofOfDeliveryDraftStore(
        mutableMapOf<String, ProofOfDeliveryDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryDraftStore(
    var inner: ProofOfDeliveryDraftStore,
    var cache: TtlCache<ProofOfDeliveryDraft>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryDraftStore {
    override fun find(code: String): ProofOfDeliveryDraft? {
        val hit = (cache.get(code) as ProofOfDeliveryDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryDraft> = inner.list()
}

fun newCachedProofOfDeliveryDraftStore(inner: ProofOfDeliveryDraftStore, ttl: Int, clock: Clock): ProofOfDeliveryDraftStore {
    return CachedProofOfDeliveryDraftStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery drafts. Empty fields match all. */
data class ProofOfDeliveryDraftQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryDraft>): MutableList<ProofOfDeliveryDraft> {
        val matched = filter(items) { candidate: ProofOfDeliveryDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryDrafts(store: ProofOfDeliveryDraftStore, query: ProofOfDeliveryDraftQuery): MutableList<ProofOfDeliveryDraft> {
    return query.apply(store.list())
}
