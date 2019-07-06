package com.patho.main.action.dialog.task;

import com.patho.main.action.dialog.AbstractDialog;
import com.patho.main.common.Dialog;
import com.patho.main.model.MaterialPreset;
import com.patho.main.model.patient.Task;
import com.patho.main.repository.MaterialPresetRepository;
import com.patho.main.service.SampleService;
import com.patho.main.ui.transformer.DefaultTransformer;
import com.patho.main.util.dialog.event.StainingPhaseUpdateEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

@Configurable
@Getter
@Setter
public class CreateSampleDialog extends AbstractDialog {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SampleService sampleService;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MaterialPresetRepository materialPresetRepository;

	private List<MaterialPreset> materials;

	private DefaultTransformer<MaterialPreset> materialTransformer;

	private MaterialPreset selectedMaterial;

	public CreateSampleDialog initAndPrepareBean(Task task) {
		if (initBean(task))
			prepareDialog();

		return this;
	}

	public boolean initBean(Task task) {
		super.initBean(task, Dialog.SAMPLE_CREATE);

		setMaterials(materialPresetRepository.findAll(true));

		if (!getMaterials().isEmpty()) {
			setMaterialTransformer(new DefaultTransformer<>(getMaterials()));
		}

		return true;
	}

	public void createSampleAndHide() {
		hideDialog(new StainingPhaseUpdateEvent(sampleService.createSample(getTask(), getSelectedMaterial())));
	}
}
