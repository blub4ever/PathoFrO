package com.patho.main.repository.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.patho.main.model.Physician_;
import com.patho.main.model.StainingPrototype;
import com.patho.main.model.StainingPrototype_;
import com.patho.main.model.StainingPrototype.StainingType;
import com.patho.main.repository.service.StainingPrototypeRepositoryCustom;

@Service
@Transactional
public class StainingPrototypeRepositoryImpl extends AbstractRepositoryCustom
		implements StainingPrototypeRepositoryCustom {

	public Optional<StainingPrototype> findOptionalByIdAndInitilize(Long id, boolean initializeBatch) {
		CriteriaQuery<StainingPrototype> criteria = getCriteriaBuilder().createQuery(StainingPrototype.class);
		Root<StainingPrototype> root = criteria.from(StainingPrototype.class);

		criteria.select(root);

		if (initializeBatch)
			root.fetch(StainingPrototype_.batchDetails, JoinType.LEFT);

		criteria.where(getCriteriaBuilder().equal(root.get(StainingPrototype_.id), id));

		criteria.distinct(true);

		List<StainingPrototype> stainingPrototypes = getSession().createQuery(criteria).getResultList();

		return Optional.ofNullable(stainingPrototypes.size() > 0 ? stainingPrototypes.get(0) : null);
	}

	public List<StainingPrototype> findAllByTypeIgnoreArchivedOrderByPriorityCountDesc(StainingType type,
			boolean initializeBatch, boolean irgnoreArchived) {
		CriteriaQuery<StainingPrototype> criteria = getCriteriaBuilder().createQuery(StainingPrototype.class);
		Root<StainingPrototype> root = criteria.from(StainingPrototype.class);

		criteria.select(root);

		if (initializeBatch)
			root.fetch(StainingPrototype_.batchDetails, JoinType.LEFT);

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(getCriteriaBuilder().equal(root.get(StainingPrototype_.type), type));

		if (irgnoreArchived)
			predicates.add(getCriteriaBuilder().equal(root.get(StainingPrototype_.archived), false));

		criteria.where(getCriteriaBuilder().and(predicates.toArray(new Predicate[predicates.size()])));

		criteria.orderBy(getCriteriaBuilder().desc(root.get(StainingPrototype_.priorityCount)));

		criteria.distinct(true);

		return getSession().createQuery(criteria).getResultList();
	}
}